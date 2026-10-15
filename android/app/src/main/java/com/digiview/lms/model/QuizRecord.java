package com.digiview.lms.model;

/**
 * Android QuizRecord inheriting from the shared Universal QuizRecord blueprint.
 */
public class QuizRecord extends com.digiview.core.model.QuizRecord {
    public QuizRecord() {
        super();
    }

    public QuizRecord(String username, String quiz, int score, int total, int percent, String date) {
        super(username, quiz, score, total, percent, date);
    }
}

