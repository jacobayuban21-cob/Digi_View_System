package com.digiview.core.model;

import java.io.Serializable;

/**
 * Universal Quiz Attempt Record shared by Desktop Swing, Android Mobile, and Cloud Backend.
 */
public class QuizRecord implements Serializable {
    private static final long serialVersionUID = 1L;

    public Long id;
    public String username;
    public String quiz;
    public int score;
    public int total;
    public int percent;
    public String date;

    public QuizRecord() {
    }

    public QuizRecord(String quiz, int score, int total, int percent, String date) {
        this("", quiz, score, total, percent, date);
    }

    public QuizRecord(String username, String quiz, int score, int total, int percent, String date) {
        this.username = username != null ? username : "";
        this.quiz = quiz != null ? quiz : "";
        this.score = score;
        this.total = total;
        this.percent = percent;
        this.date = date != null ? date : "";
    }

    public boolean isPassed() {
        return percent >= 70;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getQuiz() {
        return quiz;
    }

    public void setQuiz(String quiz) {
        this.quiz = quiz;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getPercent() {
        return percent;
    }

    public void setPercent(int percent) {
        this.percent = percent;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
