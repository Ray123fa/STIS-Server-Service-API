package com.polstat.penyediaanserver.auth;

import com.polstat.penyediaanserver.dto.UserDto;
import com.polstat.penyediaanserver.enums.Role;
import com.polstat.penyediaanserver.service.UserService;
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

@RestController
public class AuthController {
    @Autowired
    AuthenticationManager authManager;

    @Autowired
    JwtUtil jwtUtil;

    @Autowired
    UserService userService;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody @Valid AuthRequest request) {
        try {
            Authentication authentication =
                    authManager.authenticate(new UsernamePasswordAuthenticationToken(
                            request.getEmail(), request.getPassword()));

            String accessToken = jwtUtil.generateAccessToken(authentication);
            AuthResponse response = new AuthResponse(request.getEmail(), accessToken);
            return ResponseEntity.ok().body(response);
        } catch (BadCredentialsException ex) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody UserDto userDto) {
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
