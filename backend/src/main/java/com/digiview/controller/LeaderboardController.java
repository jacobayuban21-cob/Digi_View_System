package com.digiview.controller;

import com.digiview.dto.ApiResponse;
import com.digiview.dto.LeaderboardEntryDto;
import com.digiview.entity.UserEntity;
import com.digiview.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/leaderboard")
public class LeaderboardController {

    private final UserRepository userRepository;

    public LeaderboardController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<LeaderboardEntryDto>>> getLeaderboard() {
        List<UserEntity> topUsers = userRepository.findTop50ByOrderByXpDesc();
        List<LeaderboardEntryDto> entries = new ArrayList<>();
        int rank = 1;
        for (UserEntity u : topUsers) {
            entries.add(new LeaderboardEntryDto(rank++, u.getFullName(), u.getUsername(), u.getGradeSection(), u.getXp()));
        }
        return ResponseEntity.ok(ApiResponse.ok(entries));
    }
}
