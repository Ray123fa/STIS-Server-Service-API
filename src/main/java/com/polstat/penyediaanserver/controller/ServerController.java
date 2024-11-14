package com.polstat.penyediaanserver.controller;

import com.polstat.penyediaanserver.dto.ServerDto;
import com.polstat.penyediaanserver.entity.Server;
import com.polstat.penyediaanserver.entity.ServerAccount;
import com.polstat.penyediaanserver.entity.User;
import com.polstat.penyediaanserver.enums.ServerStatus;
import com.polstat.penyediaanserver.exception.ServerRequestNotExistException;
import com.polstat.penyediaanserver.repository.ServerRepository;
import com.polstat.penyediaanserver.repository.UserRepository;
import com.polstat.penyediaanserver.service.ServerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/server")
public class ServerController {
    @Autowired
    private ServerService serverService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServerRepository serverRepository;

    @PostMapping("/request")
    @PreAuthorize("hasRole('MAHASISWA')")
    public ResponseEntity<Map<String, String>> requestServerAccess(@RequestBody ServerDto serverDto,
                                                                   @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername());
        serverService.requestServerAccess(serverDto, user);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Permintaan akses server berhasil diajukan.");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/requests")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Map<String, Object>> getServerRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            @RequestParam(required = false) String filtered) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Server> serverRequests;
        if (filtered != null) {
            try {
                ServerStatus status = ServerStatus.valueOf(filtered.toUpperCase());
                serverRequests = serverRepository.findByStatus(status, pageable);
            } catch (IllegalArgumentException e) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Status filter tidak valid: " + filtered);
                response.put("status", "error");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        } else {
            serverRequests = serverRepository.findAll(pageable);
        }

        List<Map<String, Object>> filteredData = serverRequests.getContent().stream().map(server -> {
            Map<String, Object> serverData = new HashMap<>();
            serverData.put("id", server.getId());

            Map<String, Object> ownerData = new HashMap<>();
            ownerData.put("id", server.getOwner().getId());
            ownerData.put("name", server.getOwner().getName());
            ownerData.put("email", server.getOwner().getEmail());
            serverData.put("owner", ownerData);

            serverData.put("purpose", server.getPurpose());
            serverData.put("status", server.getStatus());
            return serverData;
        }).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", filteredData);
        response.put("page", serverRequests.getNumber());
        response.put("totalPages", serverRequests.getTotalPages());
        response.put("totalElements", serverRequests.getTotalElements());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/my-requests")
    @PreAuthorize("hasRole('MAHASISWA')")
    public ResponseEntity<Map<String, Object>> getMyServerRequests(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        User user = userRepository.findByEmail(userDetails.getUsername());

        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Server> serverRequests = serverService.getServerRequestsByOwner(user, pageable);

        List<Map<String, Object>> filteredData = serverRequests.getContent().stream().map(server -> {
            Map<String, Object> serverData = new HashMap<>();
            serverData.put("id", server.getId());
            serverData.put("ownerId", server.getOwner().getId());
            serverData.put("purpose", server.getPurpose());
            serverData.put("status", server.getStatus());
            return serverData;
        }).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", filteredData);
        response.put("page", serverRequests.getNumber());
        response.put("totalPages", serverRequests.getTotalPages());
        response.put("totalElements", serverRequests.getTotalElements());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PatchMapping("/request/{id}/approve")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Map<String, Object>> approveServerRequest(@PathVariable Long id) {
        return serverService.getServerById(id)
                .map(server -> {
                    if (server.getStatus() == ServerStatus.APPROVED) {
                        Map<String, Object> response = new HashMap<>();
                        response.put("status", "error");
                        response.put("message", "Permintaan server sudah disetujui sebelumnya.");
                        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                    }

                    serverService.approveServerRequest(server);
                    User user = server.getOwner();
                    ServerAccount serverAccount = serverService.createServerAccount(server, user);

                    Map<String, Object> response = new HashMap<>();
                    response.put("status", "success");
                    response.put("message", "Permintaan server berhasil disetujui.");
                    response.put("account", Map.of(
                            "username", serverAccount.getUsername(),
                            "password", serverAccount.getPassword()
                    ));
                    return new ResponseEntity<>(response, HttpStatus.OK);
                })
                .orElseGet(() -> {
                    Map<String, Object> response = new HashMap<>();
                    response.put("status", "error");
                    response.put("message", "Pengajuan server tidak ditemukan.");
                    return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
                });
    }

    @PatchMapping("/request/{id}/reject")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Map<String, String>> rejectServerRequest(@PathVariable Long id, @RequestBody(required = false) Map<String, String> requestBody) {
        String reason = (requestBody != null && requestBody.containsKey("reason"))
                ? requestBody.get("reason")
                : "Tidak ada alasan yang diberikan";

        return serverService.getServerById(id)
                .map(server -> {
                    if (server.getStatus() == ServerStatus.APPROVED) {
                        Map<String, String> response = new HashMap<>();
                        response.put("status", "error");
                        response.put("message", "Permintaan server telah disetujui sebelumnya, tidak dapat diubah.");
                        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                    }

                    serverService.rejectServerRequest(server, reason);
                    Map<String, String> response = new HashMap<>();
                    response.put("status", "success");
                    response.put("message", "Permintaan server berhasil ditolak.");
                    return new ResponseEntity<>(response, HttpStatus.OK);
                })
                .orElseGet(() -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("status", "error");
                    response.put("message", "Pengajuan server tidak ditemukan.");
                    return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
                });
    }

    @GetMapping("/request/{id}")
    public ResponseEntity<Map<String, Object>> getServerRequestDetail(@PathVariable Long id) {
        Server server = serverService.getServerById(id)
                .orElseThrow(() -> new ServerRequestNotExistException("Permintaan server tidak ditemukan."));

        Map<String, Object> serverData = new HashMap<>();
        serverData.put("id", server.getId());

        Map<String, Object> ownerData = new HashMap<>();
        ownerData.put("id", server.getOwner().getId());
        ownerData.put("name", server.getOwner().getName());
        ownerData.put("email", server.getOwner().getEmail());
        serverData.put("owner", ownerData);

        serverData.put("purpose", server.getPurpose());
        serverData.put("status", server.getStatus());

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", serverData);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PatchMapping("/request/{id}")
    @PreAuthorize("hasRole('MAHASISWA')")
    public ResponseEntity<Map<String, String>> updateServerRequest(
            @PathVariable Long id,
            @RequestBody ServerDto serverDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername());

        return serverService.getServerById(id)
                .map(server -> {
                    if (!server.getOwner().getId().equals(user.getId())) {
                        Map<String, String> response = new HashMap<>();
                        response.put("status", "error");
                        response.put("message", "Anda tidak memiliki izin untuk memperbarui pengajuan server ini.");
                        return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
                    }

                    if (!"pending".equalsIgnoreCase(String.valueOf(server.getStatus()))) {
                        Map<String, String> response = new HashMap<>();
                        response.put("status", "error");
                        response.put("message", "Server hanya bisa diedit selama statusnya masih pending.");
                        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                    }

                    server.setPurpose(serverDto.getPurpose());
                    serverRepository.save(server);

                    Map<String, String> response = new HashMap<>();
                    response.put("status", "success");
                    response.put("message", "Server berhasil diperbarui.");
                    return new ResponseEntity<>(response, HttpStatus.OK);
                })
                .orElseGet(() -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("status", "error");
                    response.put("message", "Server tidak ditemukan.");
                    return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
                });
    }

    @PatchMapping("/release/{id}")
    @PreAuthorize("hasRole('MAHASISWA')")
    public ResponseEntity<Map<String, String>> releaseServer(@PathVariable Long id) {
        return serverService.getServerById(id)
                .map(server -> {
                    serverService.releaseServer(server);
                    Map<String, String> response = new HashMap<>();
                    response.put("status", "success");
                    response.put("message", "Server berhasil dilepaskan.");
                    return new ResponseEntity<>(response, HttpStatus.OK);
                })
                .orElseGet(() -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("status", "error");
                    response.put("message", "Server tidak ditemukan.");
                    return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
                });
    }

    @DeleteMapping("/terminate/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Map<String, String>> terminateServer(@PathVariable Long id) {
        return serverService.getServerById(id)
                .map(server -> {
                    if (!server.getStatus().equals(ServerStatus.RELEASED)) {
                        Map<String, String> response = new HashMap<>();
                        response.put("status", "error");
                        response.put("message", "Server hanya dapat dinonaktifkan/dihapus jika statusnya 'RELEASED'.");
                        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
                    }

                    serverService.terminateServer(server);
                    Map<String, String> response = new HashMap<>();
                    response.put("status", "success");
                    response.put("message", "Server berhasil dinonaktifkan.");
                    return new ResponseEntity<>(response, HttpStatus.OK);
                })
                .orElseGet(() -> {
                    Map<String, String> response = new HashMap<>();
                    response.put("status", "error");
                    response.put("message", "Server tidak ditemukan.");
                    return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
                });
    }
}