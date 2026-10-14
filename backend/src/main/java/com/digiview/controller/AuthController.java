package com.digiview.controller;

import com.digiview.dto.*;
import com.digiview.entity.UserEntity;
import com.digiview.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;

    public AuthController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserEntity>> register(@RequestBody RegisterRequest req) {
        if (req.getUsername() == null || req.getUsername().trim().isEmpty() ||
            req.getPassword() == null || req.getPassword().trim().isEmpty()) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Username and password are required."));
        }

        String username = req.getUsername().trim().toLowerCase();
        if (userRepository.existsByUsernameIgnoreCase(username)) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Username is already taken."));
        }

        UserEntity user = new UserEntity(
            req.getFullName() != null ? req.getFullName().trim() : username,
            req.getAge() > 0 ? req.getAge() : 16,
            req.getGender() != null ? req.getGender() : "Not specified",
            req.getStrand() != null ? req.getStrand() : "General",
            req.getGradeSection() != null ? req.getGradeSection() : "Default",
            username,
            req.getPassword() // In production, hash with BCryptPasswordEncoder
        );

        UserEntity saved = userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.ok("Registration successful!", saved));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserEntity>> login(@RequestBody AuthRequest req) {
        if (req.getUsername() == null || req.getPassword() == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Username and password are required."));
        }

        Optional<UserEntity> userOpt = userRepository.findByUsernameIgnoreCase(req.getUsername().trim());
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(ApiResponse.error("User does not exist."));
        }

        UserEntity user = userOpt.get();
        if (!user.getPassword().equals(req.getPassword())) {
            return ResponseEntity.status(401).body(ApiResponse.error("Invalid password."));
        }

        return ResponseEntity.ok(ApiResponse.ok("Login successful!", user));
    }
}
