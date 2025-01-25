package com.polstat.penyediaanserver.controller;

import com.polstat.penyediaanserver.auth.AuthRequest;
import com.polstat.penyediaanserver.auth.AuthResponse;
import com.polstat.penyediaanserver.auth.JwtUtil;
import com.polstat.penyediaanserver.dto.UserDto;
import com.polstat.penyediaanserver.enums.Role;
import com.polstat.penyediaanserver.resreq.RegisterResponse;
import com.polstat.penyediaanserver.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@Tag(name = "Auth Controller")
@RestController
public class AuthController {
    @Autowired
    AuthenticationManager authManager;

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    UserService userService;

    @Operation(summary = "Login user", description = "Authenticates user and returns JWT token upon successful login.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful, returns access token", content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = AuthResponse.class)
            )),
            @ApiResponse(responseCode = "400", description = "Bad credentials", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(example = "{ \"status\": \"error\", \"message\": \"Email atau password salah.\" }")
            ))
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @Schema(
            example = "{ \"email\": \"rehan@stis.ac.id\", \"password\": \"rehan\" }"
    ) @RequestBody AuthRequest request) {
        try {
            Authentication authentication =
                    authManager.authenticate(new UsernamePasswordAuthenticationToken(
                            request.getEmail(), request.getPassword()));

            // Ambil informasi pengguna dari database berdasarkan email
            UserDto user = userService.getUserByEmail(request.getEmail());

            // Generate JWT token
            String accessToken = jwtUtil.generateAccessToken(authentication);

            // Buat respons autentikasi dengan nama dan role
            AuthResponse response = new AuthResponse(user.getName(), user.getEmail(), user.getRole().name(), accessToken);

            return ResponseEntity.ok().body(response);
        } catch (BadCredentialsException ex) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", "error");
            response.put("message", "Email atau password salah.");
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @Operation(summary = "Register user", description = "Registers a new user with email validation and returns created user data.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Registration successful", content = @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = RegisterResponse.class)
            )),
            @ApiResponse(
                    responseCode = "400",
                    description = "Failed registration",
                    content = @Content(mediaType = "application/json",
                            schema = @Schema(example = "{ \"status\": \"error\", \"message\": \"Gagal melakukan registrasi: Email sudah terdaftar. Gunakan email lain.\" }"))
            )
    })
    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @Schema(
            example = "{ \"name\": \"Rehan\", \"email\": \"rehan@stis.ac.id\", \"password\": \"rehan\" }"
    ) @RequestBody UserDto userDto) {
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
            UserDto createdUser = userService.createUser(userDto, Role.MAHASISWA);
            response.put("status", "success");
            response.put("message", "Registrasi berhasil dilakukan.");
            response.put("data", createdUser);

            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            response.put("status", "error");
            response.put("message", "Gagal melakukan registrasi: " + e.getMessage());

            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
}
