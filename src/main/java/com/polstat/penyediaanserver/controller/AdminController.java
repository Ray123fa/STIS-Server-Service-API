package com.polstat.penyediaanserver.controller;

import com.polstat.penyediaanserver.dto.UserDto;
import com.polstat.penyediaanserver.entity.User;
import com.polstat.penyediaanserver.enums.Role;
import com.polstat.penyediaanserver.repository.UserRepository;
import com.polstat.penyediaanserver.service.UserService;
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
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMINISTRATOR')")
public class AdminController {
    @Autowired
    private UserService userService;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/list-user")
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

    @GetMapping("/list-mahasiswa")
    public ResponseEntity<Map<String, Object>> getStudents(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<User> students = userRepository.findAllByRole(Role.MAHASISWA, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", students.getContent());
        response.put("page", students.getNumber());
        response.put("totalPages", students.getTotalPages());
        response.put("totalElements", students.getTotalElements());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @GetMapping("/list-administrator")
    public ResponseEntity<Map<String, Object>> getAdministrators(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort sort = direction.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);

        Page<User> administrators = userRepository.findAllByRole(Role.ADMINISTRATOR, pageable);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "success");
        response.put("data", administrators.getContent());
        response.put("page", administrators.getNumber());
        response.put("totalPages", administrators.getTotalPages());
        response.put("totalElements", administrators.getTotalElements());

        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    @PostMapping("/add")
    public ResponseEntity<Map<String, Object>> addAdministrator(@Valid @RequestBody UserDto userDto) {
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

    @DeleteMapping("/delete-user")
    public ResponseEntity<Map<String, String>> deleteUserByAdmin(@RequestParam String email, @AuthenticationPrincipal UserDetails userDetails) {
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
            response.put("message", "Pengguna dengan email tersebut tidak ditemukan.");
            return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
    }
}