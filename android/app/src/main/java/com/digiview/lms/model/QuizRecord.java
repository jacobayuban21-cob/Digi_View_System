package com.digiview.lms.model;

import java.io.Serializable;

public class QuizRecord implements Serializable {
    public Long id;
    public String username;
    public String quiz;
    public int score;
    public int total;
    public int percent;
    public String date;

    public QuizRecord() {}

    public QuizRecord(String username, String quiz, int score, int total, int percent, String date) {
        this.username = username;
        this.quiz = quiz;
        this.score = score;
        this.total = total;
        this.percent = percent;
        this.date = date;
    }
}
