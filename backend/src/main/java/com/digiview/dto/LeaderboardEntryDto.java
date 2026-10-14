package com.digiview.dto;

public class LeaderboardEntryDto {
    private int rank;
    private String fullName;
    private String username;
    private String gradeSection;
    private int xp;
    private int level;

    public LeaderboardEntryDto() {}

    public LeaderboardEntryDto(int rank, String fullName, String username, String gradeSection, int xp) {
        this.rank = rank;
        this.fullName = fullName;
        this.username = username;
        this.gradeSection = gradeSection;
        this.xp = xp;
        this.level = (xp / 100) + 1;
    }

    public int getRank() { return rank; }
    public void setRank(int rank) { this.rank = rank; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getGradeSection() { return gradeSection; }
    public void setGradeSection(String gradeSection) { this.gradeSection = gradeSection; }

    public int getXp() { return xp; }
    public void setXp(int xp) { this.xp = xp; }

    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
}
