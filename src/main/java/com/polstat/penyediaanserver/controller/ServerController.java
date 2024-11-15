package com.polstat.penyediaanserver.controller;

import com.polstat.penyediaanserver.dto.ServerDto;
import com.polstat.penyediaanserver.entity.Server;
import com.polstat.penyediaanserver.entity.ServerAccount;
import com.polstat.penyediaanserver.entity.User;
import com.polstat.penyediaanserver.enums.ServerStatus;
import com.polstat.penyediaanserver.exception.ServerRequestNotExistException;
import com.polstat.penyediaanserver.repository.ServerRepository;
import com.polstat.penyediaanserver.repository.UserRepository;
import com.polstat.penyediaanserver.resreq.ApprovalServerResponse;
import com.polstat.penyediaanserver.resreq.GetMyServersResponse;
import com.polstat.penyediaanserver.resreq.GetOneServerResponse;
import com.polstat.penyediaanserver.resreq.GetServersResponse;
import com.polstat.penyediaanserver.service.ServerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(name = "Server Controller")
@RestController
@RequestMapping("/api/server")
public class ServerController {
    @Autowired
    private ServerService serverService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ServerRepository serverRepository;

    @Operation(summary = "Request server access",
            description = "Request access to a server. Only students can access this endpoint.")
    @ApiResponses(value = {@ApiResponse(responseCode = "201",
            description = "Server access request submitted successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(
                    example = "{ \"status\": \"success\", \"message\": \"Permintaan akses server berhasil diajukan.\" }"))),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden for non-student users",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(
                                    example = "{ \"status\": \"error\", \"message\": \"Kamu tidak memiliki akses.\" }")))})
    @PostMapping("/request")
    @PreAuthorize("hasRole('MAHASISWA')")
    public ResponseEntity<Map<String, String>> requestServerAccess(@Schema(
            example = "{ \"purpose\": \"Hosting project\" }") @RequestBody ServerDto serverDto,
                                                                   @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername());
        serverService.requestServerAccess(serverDto, user);

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Permintaan akses server berhasil diajukan.");
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Get all server requests with optional status filter",
            description = "Retrieve all server requests with pagination, sorting, and an optional filter by status. Only administrators can access this endpoint.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "List of server requests retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(
                    implementation = GetServersResponse.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid status filter provided",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(
                                    example = "{ \"status\": \"error\", \"message\": \"Filter status tidak valid.\" }")))})
    @GetMapping("/requests")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Map<String, Object>> getServerRequests(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            @Parameter(description = "Filter by server request status, this parameter is optional.",
                    schema = @Schema(allowableValues = {"PENDING", "APPROVED",
                            "REJECTED", "RELEASED"})) @RequestParam(
                    required = false) String status) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Server> serverRequests;
        if (status != null) {
            try {
                ServerStatus _status = ServerStatus.valueOf(status.toUpperCase());
                serverRequests = serverRepository.findByStatus(_status, pageable);
            } catch (IllegalArgumentException e) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Filter status tidak valid.");
                response.put("status", "error");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        } else {
            serverRequests = serverRepository.findAll(pageable);
        }

        List<Map<String, Object>> filteredData =
                serverRequests.getContent().stream().map(server -> {
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

    @Operation(summary = "Get user's server requests with optional status filter",
            description = "Retrieve own server requests with pagination, sorting, and an optional filter by status")
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "List of user's server requests retrieved successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(
                    implementation = GetMyServersResponse.class))),
            @ApiResponse(responseCode = "400",
                    description = "Invalid status filter provided",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(
                                    example = "{ \"status\": \"error\", \"message\": \"Filter status tidak valid.\" }"))),
            @ApiResponse(responseCode = "403",
                    description = "Forbidden for non-student users",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(
                                    example = "{ \"status\": \"error\", \"message\": \"Kamu tidak memiliki akses.\" }")))})
    @GetMapping("/my-requests")
    @PreAuthorize("hasRole('MAHASISWA')")
    public ResponseEntity<Map<String, Object>> getMyServerRequests(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction,
            @Parameter(description = "Filter by server request status, this parameter is optional.",
                    schema = @Schema(allowableValues = {"PENDING", "APPROVED",
                            "REJECTED", "RELEASED"})) @RequestParam(
                    required = false) String status) {
        User user = userRepository.findByEmail(userDetails.getUsername());

        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<Server> serverRequests;
        if (status != null) {
            try {
                ServerStatus _status = ServerStatus.valueOf(status.toUpperCase());
                serverRequests = serverRepository.findByStatusAndOwner(_status,
                        user, pageable);
            } catch (IllegalArgumentException e) {
                Map<String, Object> response = new HashMap<>();
                response.put("message", "Filter status tidak valid.");
                response.put("status", "error");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
        } else {
            serverRequests = serverRepository.findByOwner(user, pageable);
        }

        List<Map<String, Object>> filteredData =
                serverRequests.getContent().stream().map(server -> {
                    Map<String, Object> serverData = new HashMap<>();
                    serverData.put("id", server.getId());
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

    @Operation(summary = "Approve server request",
            description = "Approve a server access request. Only administrators can approve requests.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Server request approved successfully", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = ApprovalServerResponse.class)
            )),
            @ApiResponse(responseCode = "400", description = "Request already approved",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(
                                    example = "{ \"status\": \"error\", \"message\": \"Pengajuan server sudah disetujui sebelumnya.\" }"))),
            @ApiResponse(responseCode = "404", description = "Server request not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(
                                    example = "{ \"status\": \"error\", \"message\": \"Pengajuan server tidak ditemukan.\" }")))})
    @PatchMapping("/request/{id}/approve")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Map<String, Object>> approveServerRequest(
            @Parameter(description = "The unique ID of the server request",
                    required = true) @PathVariable Long id) {
        return serverRepository.findById(id).map(server -> {
            if (server.getStatus() == ServerStatus.APPROVED) {
                Map<String, Object> response = new HashMap<>();
                response.put("status", "error");
                response.put("message",
                        "Pengajuan server sudah disetujui sebelumnya.");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            serverService.approveServerRequest(server);
            User user = server.getOwner();
            ServerAccount serverAccount =
                    serverService.createServerAccount(server, user);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Pengajuan server berhasil disetujui.");
            response.put("account", Map.of("username", serverAccount.getUsername(),
                    "password", serverAccount.getPassword()));
            return new ResponseEntity<>(response, HttpStatus.OK);
        }).orElseGet(() -> {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Pengajuan server tidak ditemukan.");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        });
    }

    @Operation(summary = "Reject server request",
            description = "Reject a server access request with an optional reason. Only administrators can reject requests.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "Server request rejected successfully",
            content = @Content(mediaType = "application/json", schema = @Schema(
                    example = "{ \"status\": \"success\", \"message\": \"Pengajuan server berhasil ditolak.\" }"))),
            @ApiResponse(responseCode = "400", description = "Request already approved",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(
                                    example = "{ \"status\": \"error\", \"message\": \"Pengajuan server telah disetujui sebelumnya, tidak dapat diubah.\" }"))),
            @ApiResponse(responseCode = "404", description = "Server request not found",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(
                                    example = "{ \"status\": \"error\", \"message\": \"Pengajuan server tidak ditemukan.\" }")))})
    @PatchMapping("/request/{id}/reject")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Map<String, String>> rejectServerRequest(
            @Parameter(description = "The unique ID of the server request",
                    required = true) @PathVariable Long id,
            @RequestBody(required = false) Map<String, String> requestBody) {
        String reason = (requestBody != null && requestBody.containsKey("reason"))
                ? requestBody.get("reason")
                : "Tidak ada alasan yang diberikan";

        return serverRepository.findById(id).map(server -> {
            if (server.getStatus() == ServerStatus.APPROVED) {
                Map<String, String> response = new HashMap<>();
                response.put("status", "error");
                response.put("message",
                        "Pengajuan server telah disetujui sebelumnya, tidak dapat diubah.");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            serverService.rejectServerRequest(server, reason);
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Pengajuan server berhasil ditolak.");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }).orElseGet(() -> {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Pengajuan server tidak ditemukan.");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        });
    }

    @Operation(summary = "Get server request details",
            description = "Retrieve detailed information about a specific server request by its ID.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Server request details retrieved successfully", content = @Content(
                    mediaType = "application/json", schema = @Schema(implementation = GetOneServerResponse.class)
            )),
            @ApiResponse(responseCode = "403", description = "Only authorized users have permission to access this request detail.", content = @Content(
                    mediaType = "application/json", schema = @Schema(example = "{ \"status\": \"error\", \"message\": \"Anda tidak memiliki izin untuk mengakses detail pengajuan ini.\" }")
            )),
            @ApiResponse(responseCode = "404", description = "Server request not found", content = @Content(
                    mediaType = "application/json", schema = @Schema(example = "{ \"status\": \"error\", \"message\": \"Pengajuan server tidak ditemukan.\" }")
            ))
    })
    @GetMapping("/request/{id}")
    public ResponseEntity<Map<String, Object>> getServerRequestDetail(
            @Parameter(description = "The unique ID of the server request", required = true)
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        Server server = serverRepository.findById(id)
                .orElseThrow(() -> new ServerRequestNotExistException(
                        "Pengajuan server tidak ditemukan."));

        // Ambil username dan peran pengguna dari userDetails
        String currentUsername = userDetails.getUsername();
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMINISTRATOR"));

        // Jika pengguna bukan administrator, verifikasi bahwa mereka hanya mengakses datanya sendiri
        if (!isAdmin) {
            // Pastikan pemilik pengajuan server adalah pengguna yang sedang login
            if (!server.getOwner().getEmail().equalsIgnoreCase(currentUsername)) {
                Map<String, Object> response = new HashMap<>();
                response.put("status", "error");
                response.put("message", "Anda tidak memiliki izin untuk mengakses detail pengajuan ini.");
                return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
            }
        }

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

    @Operation(summary = "Update server request",
            description = "Update an existing server request's purpose. Only the owner can update their request if it is still pending.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200",description = "Server request updated successfully", content = @Content(
                    mediaType = "application/json", schema = @Schema(example = "{ \"status\": \"success\", \"message\": \"Pengajuan server berhasil diperbarui.\" }")
            )),
            @ApiResponse(responseCode = "403", description = "Forbidden for non-owner users", content = @Content(
                    mediaType = "application/json", schema = @Schema(example = "{ \"status\": \"error\", \"message\": \"Anda tidak memiliki izin untuk memperbarui pengajuan server ini.\" }")
            )),
            @ApiResponse(responseCode = "400", description = "Request can only be updated if it is still pending", content = @Content(
                    mediaType = "application/json", schema = @Schema(example = "{ \"status\": \"error\", \"message\": \"Pengajuan server hanya bisa diedit selama statusnya masih pending.\" }")
            )),
            @ApiResponse(responseCode = "404", description = "Server request not found", content = @Content(
                    mediaType = "application/json", schema = @Schema(example = "{ \"status\": \"error\", \"message\": \"Pengajuan server tidak ditemukan.\" }")
            ))
    })
    @PatchMapping("/request/{id}")
    @PreAuthorize("hasRole('MAHASISWA')")
    public ResponseEntity<Map<String, String>> updateServerRequest(
            @Parameter(description = "The unique ID of the server request",
                    required = true) @PathVariable Long id,
            @Schema(example = "{ \"purpose\": \"apaya\" }")
            @RequestBody ServerDto serverDto,
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = userRepository.findByEmail(userDetails.getUsername());

        return serverRepository.findById(id).map(server -> {
            if (!server.getOwner().getId().equals(user.getId())) {
                Map<String, String> response = new HashMap<>();
                response.put("status", "error");
                response.put("message",
                        "Anda tidak memiliki izin untuk memperbarui pengajuan server ini.");
                return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
            }

            if (!"pending".equalsIgnoreCase(String.valueOf(server.getStatus()))) {
                Map<String, String> response = new HashMap<>();
                response.put("status", "error");
                response.put("message",
                        "Pengajuan server hanya bisa diedit selama statusnya masih pending.");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            server.setPurpose(serverDto.getPurpose());
            serverRepository.save(server);

            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Pengajuan server berhasil diperbarui.");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }).orElseGet(() -> {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Pengajuan server tidak ditemukan.");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        });
    }

    @Operation(summary = "Release server", description = "Release a server back to the pool. Only the owner can release their server.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Server released successfully", content = @Content(
                    mediaType = "application/json", schema = @Schema(example = "{ \"status\": \"success\", \"message\": \"Server berhasil dilepaskan.\" }")
            )),
            @ApiResponse(responseCode = "404", description = "Server not found", content = @Content(
                    mediaType = "application/json", schema = @Schema(example = "{ \"status\": \"error\", \"message\": \"Server tidak ditemukan.\" }")
            ))
    })
    @PatchMapping("/release/{id}")
    @PreAuthorize("hasRole('MAHASISWA')")
    public ResponseEntity<Map<String, String>> releaseServer(
            @Parameter(description = "The unique ID of the server request",
                    required = true) @PathVariable Long id) {
        return serverRepository.findById(id).map(server -> {
            serverService.releaseServer(server);
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Server berhasil dilepaskan.");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }).orElseGet(() -> {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Server tidak ditemukan.");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        });
    }

    @Operation(summary = "Terminate server",
            description = "Terminate a server that has been released. Only administrators can terminate a server.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Server terminated successfully", content = @Content(
                    mediaType = "application/json", schema = @Schema(example = "{ \"status\": \"success\", \"message\": \"Server berhasil dinonaktifkan.\" }")
            )),
            @ApiResponse(responseCode = "400", description = "Server can only be terminated if status is 'RELEASED'", content = @Content(
                    mediaType = "application/json", schema = @Schema(example = "{ \"status\": \"error\", \"message\": \"Server hanya dapat dinonaktifkan/dihapus jika statusnya 'RELEASED'.\" }")
            )),
            @ApiResponse(responseCode = "404", description = "Server not found", content = @Content(
                    mediaType = "application/json", schema = @Schema(example = "{ \"status\": \"error\", \"message\": \"Server tidak ditemukan.\" }")
            ))
    })
    @DeleteMapping("/terminate/{id}")
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    public ResponseEntity<Map<String, String>> terminateServer(
            @Parameter(description = "The unique ID of the server request",
                    required = true) @PathVariable Long id) {
        return serverRepository.findById(id).map(server -> {
            if (!server.getStatus().equals(ServerStatus.RELEASED)) {
                Map<String, String> response = new HashMap<>();
                response.put("status", "error");
                response.put("message",
                        "Server hanya dapat dinonaktifkan/dihapus jika statusnya 'RELEASED'.");
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }

            serverService.terminateServer(server);
            Map<String, String> response = new HashMap<>();
            response.put("status", "success");
            response.put("message", "Server berhasil dinonaktifkan.");
            return new ResponseEntity<>(response, HttpStatus.OK);
        }).orElseGet(() -> {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Server tidak ditemukan.");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        });
    }
}
