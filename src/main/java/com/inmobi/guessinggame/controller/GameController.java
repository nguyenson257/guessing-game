package com.inmobi.guessinggame.controller;

import com.inmobi.guessinggame.config.GameConfig;
import com.inmobi.guessinggame.model.User;
import com.inmobi.guessinggame.repository.UserRepository;
import com.inmobi.guessinggame.security.jwt.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Game", description = "Game APIs")
public class GameController {
    @Autowired
    private GameConfig gameConfig;
    private final UserRepository userRepository;

    @Operation(summary = "Make a guess", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Guess result returned"),
            @ApiResponse(responseCode = "400", description = "Invalid number or no turns left"),
    })
    @PostMapping("/guess")
    public ResponseEntity<String> guess(
            @Parameter(description = "Number to guess (1-5)") @RequestParam int number) {
        if (number < 1 || number > 5) {
            return ResponseEntity.badRequest().body("Số phải từ 1 đến 5");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();

        if (user.getTurns() <= 0) {
            return ResponseEntity.badRequest().body("Hết lượt chơi");
        }

        user.setTurns(user.getTurns() - 1);

        int serverNumber = (int) (Math.random() * 5) + 1;
        boolean win = Math.random() < gameConfig.getWinRate();
        if (win) {
            user.setScore(user.getScore() + 1);
            userRepository.save(user);
            return ResponseEntity.ok("Đoán đúng! +1 điểm");
        }
        if (serverNumber == number) {
            do {
                serverNumber = (int) (Math.random() * 5) + 1;
            } while (serverNumber == number);
        }

        userRepository.save(user);
        return ResponseEntity.ok("Sai rồi! Số đúng là: " + serverNumber);
    }

    @Operation(summary = "Add turns to user's account", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Turns added successfully"),
    })
    @PostMapping("/add-turns")
    public ResponseEntity<String> addTurns() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        user.setTurns(user.getTurns() + 5);
        userRepository.save(user);
        return ResponseEntity.ok("Đã cộng 5 lượt chơi");
    }

    @Operation(summary = "Get current user's information", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "User information returned"),
    })
    @GetMapping("/me")
    public ResponseEntity<?> me() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = (User) authentication.getPrincipal();
        return ResponseEntity.ok(Map.of(
                "username", user.getUsername(),
                "score", user.getScore(),
                "turns", user.getTurns()
        ));
    }

    @Operation(summary = "Get leaderboard")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Leaderboard returned")
    })
    @GetMapping("/leaderboard")
    public ResponseEntity<?> leaderboard() {
        List<User> top10 = userRepository.findTop10ByOrderByScoreDesc();
        List<Object> result = top10.stream()
                .map(u -> Map.of("username", u.getUsername(), "score", u.getScore()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }
}
