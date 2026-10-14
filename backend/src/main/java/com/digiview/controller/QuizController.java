package com.digiview.controller;

import com.digiview.dto.ApiResponse;
import com.digiview.dto.QuizRecordDto;
import com.digiview.entity.QuizRecordEntity;
import com.digiview.entity.UserEntity;
import com.digiview.repository.QuizRecordRepository;
import com.digiview.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/quiz")
public class QuizController {

    private final QuizRecordRepository quizRecordRepository;
    private final UserRepository userRepository;

    public QuizController(QuizRecordRepository quizRecordRepository, UserRepository userRepository) {
        this.quizRecordRepository = quizRecordRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/record")
    public ResponseEntity<ApiResponse<QuizRecordEntity>> saveRecord(@RequestBody QuizRecordDto req) {
        if (req.getUsername() == null || req.getQuiz() == null) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Username and quiz name are required"));
        }

        QuizRecordEntity entity = new QuizRecordEntity(
            req.getUsername().trim().toLowerCase(),
            req.getQuiz(),
            req.getScore(),
            req.getTotal(),
            req.getPercent(),
            req.getDate() != null ? req.getDate() : new java.util.Date().toString()
        );
        QuizRecordEntity saved = quizRecordRepository.save(entity);

        // Automatically reward XP if passed
        if (req.getPercent() >= 70 && !req.getQuiz().endsWith("(Exited)")) {
            Optional<UserEntity> userOpt = userRepository.findByUsernameIgnoreCase(req.getUsername().trim());
            if (userOpt.isPresent()) {
                UserEntity u = userOpt.get();
                u.setXp(u.getXp() + (req.getScore() * 10));
                userRepository.save(u);
            }
        }

        return ResponseEntity.ok(ApiResponse.ok("Quiz record saved", saved));
    }

    @GetMapping("/history/{username}")
    public ResponseEntity<ApiResponse<List<QuizRecordDto>>> getHistory(@PathVariable String username) {
        List<QuizRecordEntity> records = quizRecordRepository.findByUsernameIgnoreCaseOrderByIdDesc(username);
        List<QuizRecordDto> dtos = records.stream()
                .map(r -> new QuizRecordDto(r.getUsername(), r.getQuizName(), r.getScore(), r.getTotal(), r.getPercent(), r.getAttemptDate()))
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(dtos));
    }
}
