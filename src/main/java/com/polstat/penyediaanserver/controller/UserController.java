package com.polstat.penyediaanserver.controller;

import com.polstat.penyediaanserver.dto.UserDto;
import com.polstat.penyediaanserver.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {
    @Autowired
    private UserService userService;

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

    @PutMapping("/profile")
    public ResponseEntity<Map<String, Object>> updateProfile(@AuthenticationPrincipal UserDetails userDetails, @RequestBody UserDto userDto) {
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

    @PatchMapping("/update-email")
    public ResponseEntity<Map<String, String>> updateEmail(@AuthenticationPrincipal UserDetails userDetails, @RequestBody Map<String, String> requestBody) {
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

    @PatchMapping("/update-password")
    public ResponseEntity<Map<String, String>> updatePassword(@AuthenticationPrincipal UserDetails userDetails, @RequestBody Map<String, String> requestBody) {
        if (userDetails == null || userDetails.getUsername() == null) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Unauthorized");
            return new ResponseEntity<>(response, HttpStatus.UNAUTHORIZED);
        }

        String newPassword = requestBody.get("newPassword");
        if (newPassword == null || newPassword.isEmpty()) {
            Map<String, String> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Password baru tidak boleh kosong.");
            return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }

        boolean isUpdated = userService.updateUserPassword(userDetails.getUsername(), newPassword);
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
}