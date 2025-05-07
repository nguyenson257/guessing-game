package com.inmobi.guessinggame.controller;

import com.inmobi.guessinggame.config.GameConfig;
import com.inmobi.guessinggame.dto.LoginRequest;
import com.inmobi.guessinggame.dto.LoginResponse;
import com.inmobi.guessinggame.model.User;
import com.inmobi.guessinggame.repository.UserRepository;
import com.inmobi.guessinggame.security.jwt.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication APIs")
public class AuthController {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JwtUtil jwtUtil;
    @Autowired
    private GameConfig gameConfig;

    @Operation(summary = "Register a new user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User registered successfully"),
            @ApiResponse(responseCode = "400", description = "Username already exists")
    })
    @PostMapping("/register")
    public String register(
            @Parameter(description = "Username for the new account") @RequestParam String username,
            @Parameter(description = "Password for the new account") @RequestParam String password) {
        if (userRepository.findByUsername(username).isPresent()) {
            return "Username already exists!";
        }

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .turns(gameConfig.getTurns())
                .build();

        userRepository.save(user);
        return "Registered successfully!";
    }

    @Operation(summary = "Login and get JWT token")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        User user = userRepository.findByUsername(loginRequest.getUsername())
                .orElse(null);

        if (user == null || !passwordEncoder.matches(loginRequest.getPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid credentials");
        }

        String token = jwtUtil.generateToken(user.getUsername());
        return ResponseEntity.ok(new LoginResponse(token));
    }
}