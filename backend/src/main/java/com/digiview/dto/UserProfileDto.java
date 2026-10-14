package com.digiview.dto;

import java.util.List;
import java.util.Set;

public class UserProfileDto {
    private String fullName;
    private int age;
    private String gender;
    private String strand;
    private String gradeSection;
    private String username;
    private int xp;
    private List<QuizRecordDto> history;
    private Set<String> achievements;

    public UserProfileDto() {}

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getStrand() { return strand; }
    public void setStrand(String strand) { this.strand = strand; }

    public String getGradeSection() { return gradeSection; }
    public void setGradeSection(String gradeSection) { this.gradeSection = gradeSection; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public int getXp() { return xp; }
    public void setXp(int xp) { this.xp = xp; }

    public List<QuizRecordDto> getHistory() { return history; }
    public void setHistory(List<QuizRecordDto> history) { this.history = history; }

    public Set<String> getAchievements() { return achievements; }
    public void setAchievements(Set<String> achievements) { this.achievements = achievements; }
}
