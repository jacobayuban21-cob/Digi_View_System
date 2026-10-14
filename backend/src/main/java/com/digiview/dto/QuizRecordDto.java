package com.digiview.dto;

public class QuizRecordDto {
    private String username;
    private String quiz;
    private int score;
    private int total;
    private int percent;
    private String date;

    public QuizRecordDto() {}

    public QuizRecordDto(String username, String quiz, int score, int total, int percent, String date) {
        this.username = username;
        this.quiz = quiz;
        this.score = score;
        this.total = total;
        this.percent = percent;
        this.date = date;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getQuiz() { return quiz; }
    public void setQuiz(String quiz) { this.quiz = quiz; }

    public int getScore() { return score; }
    public void setScore(int score) { this.score = score; }

    public int getTotal() { return total; }
    public void setTotal(int total) { this.total = total; }

    public int getPercent() { return percent; }
    public void setPercent(int percent) { this.percent = percent; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }
}
