package com.polstat.penyediaanserver.controller;

import com.polstat.penyediaanserver.dto.UserDto;
import com.polstat.penyediaanserver.entity.User;
import com.polstat.penyediaanserver.enums.Role;
import com.polstat.penyediaanserver.repository.UserRepository;
import com.polstat.penyediaanserver.service.UserService;
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
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMINISTRATOR')")
public class AdminController {
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/users")
    public ResponseEntity<Map<String, Object>> getUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<User> users = userRepository.findAll(pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", users.getContent());
        response.put("page", users.getNumber());
        response.put("totalPages", users.getTotalPages());
        response.put("totalElements", users.getTotalElements());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/add-admin")
    public ResponseEntity<Map<String, Object>> addAdministrator(@RequestBody UserDto userDto) {
        UserDto createdAdmin = userService.addAdministrator(userDto);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("message", "Administrator berhasil ditambahkan.");
        response.put("data", createdAdmin);

        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PatchMapping("/change-role/{userId}")
    public ResponseEntity<Map<String, String>> changeUserRole(@AuthenticationPrincipal UserDetails userDetails,
                                                              @PathVariable Long userId,
                                                              @RequestBody Map<String, String> requestBody) {
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