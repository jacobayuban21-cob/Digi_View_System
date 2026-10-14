package com.digiview.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "quiz_records")
public class QuizRecordEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String username;

    @Column(name = "quiz_name", nullable = false)
    private String quizName;

    @Column(nullable = false)
    private int score;

    @Column(nullable = false)
    private int total;

    @Column(nullable = false)
    private int percent;

    @Column(name = "attempt_date", nullable = false)
    private String attemptDate;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public QuizRecordEntity() {}

    public QuizRecordEntity(String username, String quizName, int score, int total, int percent, String attemptDate) {
        this.username = username;
        this.quizName = quizName;
        this.score = score;
        this.total = total;
        this.percent = percent;
        this.attemptDate = attemptDate;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getQuizName() { return quizName; }
    public void setQuizName(String quizName) { this.quizName = quizName; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public int getPercent() { return percent; }
    public void setPercent(int percent) { this.percent = percent; }

    public String getAttemptDate() { return attemptDate; }
    public void setAttemptDate(String attemptDate) { this.attemptDate = attemptDate; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
