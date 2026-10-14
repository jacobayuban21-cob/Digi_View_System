package com.digiview.controller;

import com.digiview.dto.*;
import com.digiview.entity.AchievementEntity;
import com.digiview.entity.QuizRecordEntity;
import com.digiview.entity.UserEntity;
import com.digiview.repository.AchievementRepository;
import com.digiview.repository.QuizRecordRepository;
import com.digiview.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserRepository userRepository;
    private final QuizRecordRepository quizRecordRepository;
    private final AchievementRepository achievementRepository;

    public UserController(UserRepository userRepository,
                          QuizRecordRepository quizRecordRepository,
                          AchievementRepository achievementRepository) {
        this.userRepository = userRepository;
        this.quizRecordRepository = quizRecordRepository;
        this.achievementRepository = achievementRepository;
    }

    @GetMapping("/{username}")
    public ResponseEntity<ApiResponse<UserProfileDto>> getProfile(@PathVariable String username) {
        Optional<UserEntity> userOpt = userRepository.findByUsernameIgnoreCase(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(ApiResponse.error("User not found"));
        }

        UserEntity user = userOpt.get();
        UserProfileDto dto = new UserProfileDto();
        dto.setFullName(user.getFullName());
        dto.setAge(user.getAge());
        dto.setGender(user.getGender());
        dto.setStrand(user.getStrand());
        dto.setGradeSection(user.getGradeSection());
        dto.setUsername(user.getUsername());
        dto.setXp(user.getXp());

        List<QuizRecordEntity> records = quizRecordRepository.findByUsernameIgnoreCaseOrderByIdDesc(username);
        List<QuizRecordDto> history = records.stream()
                .map(r -> new QuizRecordDto(r.getUsername(), r.getQuizName(), r.getScore(), r.getTotal(), r.getPercent(), r.getAttemptDate()))
                .collect(Collectors.toList());
        dto.setHistory(history);

        List<AchievementEntity> achievements = achievementRepository.findByUsernameIgnoreCase(username);
        Set<String> achSet = achievements.stream().map(AchievementEntity::getAchievementName).collect(Collectors.toSet());
        dto.setAchievements(achSet);

        return ResponseEntity.ok(ApiResponse.ok(dto));
    }

    @PostMapping("/{username}/xp")
    public ResponseEntity<ApiResponse<Integer>> addXp(@PathVariable String username, @RequestParam int amount) {
        Optional<UserEntity> userOpt = userRepository.findByUsernameIgnoreCase(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body(ApiResponse.error("User not found"));
        }
        UserEntity user = userOpt.get();
        user.setXp(user.getXp() + amount);
        userRepository.save(user);
        return ResponseEntity.ok(ApiResponse.ok("XP updated", user.getXp()));
    }

    @PostMapping("/{username}/achievement")
    public ResponseEntity<ApiResponse<String>> unlockAchievement(@PathVariable String username, @RequestParam String achievement) {
        if (!achievementRepository.existsByUsernameIgnoreCaseAndAchievementName(username, achievement)) {
            achievementRepository.save(new AchievementEntity(username.toLowerCase(), achievement));
        }
        return ResponseEntity.ok(ApiResponse.ok("Achievement unlocked: " + achievement));
    }
}
