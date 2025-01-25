package com.polstat.penyediaanserver.controller;

import com.polstat.penyediaanserver.dto.UserDto;
import com.polstat.penyediaanserver.enums.Role;
import com.polstat.penyediaanserver.resreq.GetProfileResponse;
import com.polstat.penyediaanserver.resreq.UpdateProfileResponse;
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

import java.util.HashMap;
import java.util.Map;

@Tag(name = "User Controller")
@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

    @Operation(summary = "Get user profile", description = "Retrieve profile information of the currently authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile retrieved successfully", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = GetProfileResponse.class)
            )),
            @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(example = "{ \"status\": \"error\", \"message\": \"Unauthorized\" }")
            )),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(example = "{ \"status\": \"error\", \"message\": \"Pengguna tidak ditemukan.\" }")
            ))
    })
    @GetMapping("/profile")
    public ResponseEntity<Map<String, Object>> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null || userDetails.getUsername() == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Unauthorized");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        UserDto userDto = userService.getUserByEmail(userDetails.getUsername());
        if (userDto == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Pengguna tidak ditemukan.");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", userDto);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Update user profile", description = "Update profile information of the currently authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Profile updated successfully", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = UpdateProfileResponse.class)
            )),
            @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(example = "{ \"status\": \"error\", \"message\": \"Unauthorized\" }")
            )),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(example = "{ \"status\": \"error\", \"message\": \"Pengguna tidak ditemukan.\" }")
            ))
    })
    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(@AuthenticationPrincipal UserDetails userDetails, @Valid @Schema(
            example = "{ \"name\": \"Rehan\", \"email\": \"rehan@stis.ac.id\", \"password\": \"rehan\" }"
    ) @RequestBody UserDto userDto) {
        if (userDetails == null || userDetails.getUsername() == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Unauthorized");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        UserDto existingUser = userService.getUserByEmail(userDetails.getUsername());
        if (existingUser == null) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Pengguna tidak ditemukan.");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }

        userDto.setRole(existingUser.getRole());
        UserDto updatedUser = userService.updateUser(existingUser.getId(), userDto);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Profil berhasil diperbarui.");
        response.put("data", updatedUser);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @Operation(summary = "Update email", description = "Update email address of the currently authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Email updated successfully", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(example = "{ \"status\": \"success\", \"message\": \"Email berhasil diperbarui.\" }")
            )),
            @ApiResponse(responseCode = "400", description = "Failure update email", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(example = "{ \"status\": \"error\", \"message\": \"Gagal memperbarui email.\" }")
            )),
            @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(example = "{ \"status\": \"error\", \"message\": \"Unauthorized\" }")
            ))
    })
    @PatchMapping("/update-email")
    public ResponseEntity<Map<String, String>> updateEmail(@AuthenticationPrincipal UserDetails userDetails, @Schema(
            example = "{ \"newEmail\": \"rehan@stis.ac.id\" }"
    ) @RequestBody Map<String, String> requestBody) {
        if (userDetails == null || userDetails.getUsername() == null) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Unauthorized");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        String newEmail = requestBody.get("newEmail");
        if (newEmail == null || newEmail.isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Email baru tidak boleh kosong.");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        if (!newEmail.endsWith("@stis.ac.id")) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Hanya email berdomain stis.ac.id yang diperbolehkan.");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        boolean isUpdated = userService.updateUserEmail(userDetails.getUsername(), newEmail);
        Map<String, String> response = new HashMap<>();
        if (isUpdated) {
            response.put("status", "success");
            response.put("message", "Email berhasil diperbarui.");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            response.put("status", "error");
            response.put("message", "Gagal memperbarui email.");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Update password", description = "Update password of the currently authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Password updated successfully", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(example = "{ \"status\": \"success\", \"message\": \"Password berhasil diperbarui.\" }")
            )),
            @ApiResponse(responseCode = "400", description = "Password cannot be empty", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(example = "{ \"status\": \"error\", \"message\": \"Password baru tidak boleh kosong.\" }")
            )),
            @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(example = "{ \"status\": \"error\", \"message\": \"Unauthorized\" }")
            ))
    })
    @PatchMapping("/update-password")
    public ResponseEntity<Map<String, String>> updatePassword(@AuthenticationPrincipal UserDetails userDetails, @Schema(
            example = "{ \"newPassword\": \"Admin123\" }"
    ) @RequestBody Map<String, String> requestBody) {
        if (userDetails == null || userDetails.getUsername() == null) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Unauthorized");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        String currentPassword = requestBody.get("currentPassword");
        if (currentPassword == null || currentPassword.isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Current password tidak boleh kosong.");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        String newPassword = requestBody.get("newPassword");
        if (newPassword == null || newPassword.isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Password baru tidak boleh kosong.");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        boolean isUpdated = userService.updateUserPassword(userDetails.getUsername(), currentPassword, newPassword);
        Map<String, String> response = new HashMap<>();
        if (isUpdated) {
            response.put("status", "success");
            response.put("message", "Password berhasil diperbarui.");
            return new ResponseEntity<>(response, HttpStatus.OK);
        } else {
            response.put("status", "error");
            response.put("message", "Gagal memperbarui password.");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
    }

    @Operation(summary = "Delete account", description = "Delete the account of the currently authenticated user.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Account deleted successfully", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(example = "{ \"status\": \"success\", \"message\": \"Akun berhasil dihapus.\" }")
            )),
            @ApiResponse(responseCode = "401", description = "Unauthorized access", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(example = "{ \"status\": \"error\", \"message\": \"Unauthorized\" }")
            )),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(example = "{ \"status\": \"error\", \"message\": \"Pengguna tidak ditemukan.\" }")
            ))
    })
    @PreAuthorize("hasRole('MAHASISWA')")
    @DeleteMapping("/delete-account")
    public ResponseEntity<Map<String, String>> deleteOwnAccount(@AuthenticationPrincipal UserDetails userDetails) {
        if (userDetails == null || userDetails.getUsername() == null) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Unauthorized");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        boolean isDeleted = userService.deleteUserByEmail(userDetails.getUsername());
        Map<String, String> response = new HashMap<>();
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

    @Operation(summary = "Change user role", description = "Change the role of a user. Only accessible by administrators.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Role updated successfully", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(example = "{ \"status\": \"success\", \"message\": \"Role berhasil diubah.\" }")
            )),
            @ApiResponse(responseCode = "400", description = "Invalid role", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(example = "{ \"status\": \"error\", \"message\": \"Role tidak valid. Pastikan role yang diberikan benar.\" }")
            )),
            @ApiResponse(responseCode = "403", description = "Forbidden to change own role", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(example = "{ \"status\": \"error\", \"message\": \"Admin tidak bisa mengubah role dirinya sendiri.\" }")
            )),
            @ApiResponse(responseCode = "404", description = "User not found", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(example = "{ \"status\": \"error\", \"message\": \"Pengguna tidak ditemukan.\" }")
            ))
    })
    @PreAuthorize("hasRole('ADMINISTRATOR')")
    @PatchMapping("/change-role/{userId}")
    public ResponseEntity<Map<String, String>> changeUserRole(@AuthenticationPrincipal UserDetails userDetails,
                                                              @Parameter(description = "The ID of the user whose role will be changed", required = true)
                                                              @PathVariable Long userId,
                                                              @Schema(example = "{ \"newRole\": \"ADMINISTRATOR\" }") @RequestBody Map<String, String> requestBody) {
        UserDto currentUser = userService.getUserByEmail(userDetails.getUsername());
        if (currentUser.getId().equals(userId)) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Admin tidak bisa mengubah role dirinya sendiri.");
            return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
        }

        String newRole = requestBody.get("newRole");
        if (newRole == null || newRole.isEmpty() || !Role.isValidRole(newRole)) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Role tidak valid. Pastikan role yang diberikan benar.");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        userService.updateUserRole(userId, newRole.toUpperCase());
        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Role berhasil diubah.");
        return new ResponseEntity<>(response, HttpStatus.OK);
    }
}