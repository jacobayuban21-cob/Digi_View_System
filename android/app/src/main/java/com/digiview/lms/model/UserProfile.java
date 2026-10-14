package com.digiview.lms.model;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

public class UserProfile implements Serializable {
    public String fullName;
    public int age;
    public String gender;
    public String strand;
    public String gradeSection;
    public String username;
    public int xp;
    public List<QuizRecord> history;
    public Set<String> achievements;

    public UserProfile() {}
}
