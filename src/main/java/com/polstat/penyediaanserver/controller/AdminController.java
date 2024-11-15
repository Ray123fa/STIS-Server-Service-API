package com.polstat.penyediaanserver.controller;

import com.polstat.penyediaanserver.dto.UserDto;
import com.polstat.penyediaanserver.entity.User;
import com.polstat.penyediaanserver.enums.Role;
import com.polstat.penyediaanserver.repository.UserRepository;
import com.polstat.penyediaanserver.resreq.AddAdministratorResponse;
import com.polstat.penyediaanserver.resreq.ListAdministratorsResponse;
import com.polstat.penyediaanserver.resreq.ListUsersResponse;
import com.polstat.penyediaanserver.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Tag(name = "Admin Controller")
@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMINISTRATOR')")
public class AdminController {
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @Operation(summary = "Get a list of users",
            description = "Retrieve a paginated list of all users, with optional sorting.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully retrieved list of users", content = @Content(
                    mediaType = "application/json", schema = @Schema(implementation = ListUsersResponse.class)
            )),
            @ApiResponse(responseCode = "403", description = "Only administrators can access this request", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(example = "{ \"status\": \"error\", \"message\": \"Kamu tidak memiliki akses.\" }")
            ))
    })
    @GetMapping("/list-user")
    public ResponseEntity<Map<String, Object>> getUsers(@RequestParam(defaultValue = "0") int page,
                                                        @RequestParam(defaultValue = "10") int size,
                                                        @RequestParam(defaultValue = "id") String sortBy,
                                                        @RequestParam(defaultValue = "asc") String direction) {
        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<User> users = userRepository.findAll(pageable);

        List<Object> filteredData = users.getContent().stream().map(user -> {
            List<Object> serverAccounts = user.getServerAccounts().stream().map(account -> {
                Map<String, Object> accountMap = new HashMap<>();
                accountMap.put("id", account.getId());
                accountMap.put("username", account.getUsername());
                accountMap.put("password", account.getPassword());
                return accountMap;
            }).collect(Collectors.toList());

            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("name", user.getName());
            userMap.put("email", user.getEmail());
            userMap.put("role", user.getRole());
            userMap.put("serverAccounts", serverAccounts);
            return userMap;
        }).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", filteredData);
        response.put("page", users.getNumber());
        response.put("totalPages", users.getTotalPages());
        response.put("totalElements", users.getTotalElements());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Get a list of students",
            description = "Retrieve a paginated list of users with role MAHASISWA.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "Successfully retrieved list of students", content = @Content(
            mediaType = "application/json", schema = @Schema(implementation = ListUsersResponse.class)
    )),
            @ApiResponse(responseCode = "403", description = "Only administrators can access this request", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(example = "{ \"status\": \"error\", \"message\": \"Kamu tidak memiliki akses.\" }")
            ))})
    @GetMapping("/list-mahasiswa")
    public ResponseEntity<Map<String, Object>> getStudents(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<User> students = userRepository.findAllByRole(Role.MAHASISWA, pageable);

        List<Object> filteredData = students.getContent().stream().map(user -> {
            List<Object> serverAccounts = user.getServerAccounts().stream().map(account -> {
                Map<String, Object> accountMap = new HashMap<>();
                accountMap.put("id", account.getId());
                accountMap.put("username", account.getUsername());
                accountMap.put("password", account.getPassword());
                return accountMap;
            }).collect(Collectors.toList());

            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("name", user.getName());
            userMap.put("email", user.getEmail());
            userMap.put("role", user.getRole());
            userMap.put("serverAccounts", serverAccounts);
            return userMap;
        }).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", filteredData);
        response.put("page", students.getNumber());
        response.put("totalPages", students.getTotalPages());
        response.put("totalElements", students.getTotalElements());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Get a list of administrators",
            description = "Retrieve a paginated list of users with role ADMINISTRATOR.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "Successfully retrieved list of administrators", content = @Content(
            mediaType = "application/json", schema = @Schema(implementation = ListAdministratorsResponse.class)
    )),
            @ApiResponse(responseCode = "403", description = "Only administrators can access this request", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(example = "{ \"status\": \"error\", \"message\": \"Kamu tidak memiliki akses.\" }")
            ))})
    @GetMapping("/list-administrator")
    public ResponseEntity<Map<String, Object>> getAdministrators(
            @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<User> administrators = userRepository.findAllByRole(Role.ADMINISTRATOR, pageable);

        List<Object> filteredData = administrators.getContent().stream().map(user -> {
            Map<String, Object> userMap = new HashMap<>();
            userMap.put("id", user.getId());
            userMap.put("name", user.getName());
            userMap.put("email", user.getEmail());
            userMap.put("role", user.getRole());
            return userMap;
        }).collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", filteredData);
        response.put("page", administrators.getNumber());
        response.put("totalPages", administrators.getTotalPages());
        response.put("totalElements", administrators.getTotalElements());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Add a new administrator",
            description = "Create a new administrator account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Administrator successfully created",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(implementation = AddAdministratorResponse.class))),
            @ApiResponse(responseCode = "400", description = "Failed registration",
                    content = @Content(mediaType = "application/json", schema = @Schema(
                            example = "{ \"status\": \"error\", \"message\": \"Gagal menambahkan administrator: Email sudah terdaftar. Gunakan email lain.\" }"))),
            @ApiResponse(responseCode = "403", description = "Only administrators can access this request", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(example = "{ \"status\": \"error\", \"message\": \"Kamu tidak memiliki akses.\" }")
            ))})
    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addAdministrator(@Valid @Schema(
            example = "{ \"name\": \"Rehan\", \"email\": \"rehan@stis.ac.id\", \"password\": \"rehan\" }") @RequestBody UserDto userDto) {
        Map<String, Object> response = new HashMap<>();

        if (userDto.getName() == null || userDto.getName().isEmpty()) {
            response.put("status", "error");
            response.put("message", "Nama tidak boleh kosong.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (userDto.getEmail() == null || userDto.getEmail().isEmpty()) {
            response.put("status", "error");
            response.put("message", "Email tidak boleh kosong.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        } else if (!userDto.getEmail().endsWith("@stis.ac.id")) {
            response.put("status", "error");
            response.put("message", "Hanya email berdomain stis.ac.id yang diperbolehkan.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        if (userDto.getPassword() == null || userDto.getPassword().isEmpty()) {
            response.put("status", "error");
            response.put("message", "Password tidak boleh kosong.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }

        try {
            UserDto createdAdmin = userService.createUser(userDto, Role.ADMINISTRATOR);
            response.put("status", "success");
            response.put("message", "Administrator berhasil ditambahkan.");
            response.put("data", createdAdmin);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Gagal menambahkan administrator: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @Operation(summary = "Delete a user by email",
            description = "Delete a user account by specifying the email. Admin cannot delete their own account.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account successfully deleted", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(example = "{ \"status\": \"success\", \"message\": \"Akun berhasil dihapus.\" }")
            )),
            @ApiResponse(responseCode = "403", description = "Admin tried to delete their own account", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(example = "{ \"status\": \"error\", \"message\": \"Tidak diperbolehkan menghapus akun Anda sendiri.\" }")
            )),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(example = "{ \"status\": \"error\", \"message\": \"Pengguna tidak ditemukan.\" }")
            ))
    })
    @DeleteMapping("/delete-user")
    public ResponseEntity<Map<String, String>> deleteUserByAdmin(
            @Parameter(description = "The email address of the user to delete",
                    required = true) @RequestParam String email,
            @AuthenticationPrincipal UserDetails userDetails) {
        Map<String, String> response = new HashMap<>();

        // Cek apakah email yang ingin dihapus sama dengan email admin yang sedang login
        if (userDetails.getUsername().equalsIgnoreCase(email)) {
            response.put("status", "error");
            response.put("message", "Tidak diperbolehkan menghapus akun Anda sendiri.");
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }

        boolean isDeleted = userService.deleteUserByEmail(email);
        if (isDeleted) {
            response.put("status", "success");
            response.put("message", "Akun berhasil dihapus.");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            response.put("status", "error");
            response.put("message", "Pengguna tidak ditemukan.");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }
}
