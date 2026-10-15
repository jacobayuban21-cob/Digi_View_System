package com.digiview.core.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Universal Question Blueprint shared by Desktop Swing and Android Mobile LMS.
 */
public class Question implements Serializable {
    private static final long serialVersionUID = 1L;

    public final String text;
    public final List<String> options;
    public final int answer; // 0-indexed index into options
    public final int difficulty; // 1 = Easy/Beginner, 2 = Medium/Intermediate, 3 = Hard/Advanced

    public Question(String text, List<String> options, int answer, int difficulty) {
        this.text = text != null ? text : "";
        this.options = options != null ? Collections.unmodifiableList(new ArrayList<>(options)) : Collections.emptyList();
        this.answer = answer;
        this.difficulty = difficulty;
    }

    public String getText() {
        return text;
    }

    public List<String> getOptions() {
        return options;
    }

    public int getAnswer() {
        return answer;
    }

    public int getDifficulty() {
        return difficulty;
    }

    public boolean isCorrect(int chosenIndex) {
        return chosenIndex == answer;
    }
}
