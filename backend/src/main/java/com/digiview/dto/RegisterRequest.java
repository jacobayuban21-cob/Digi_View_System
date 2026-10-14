package com.digiview.dto;

public class RegisterRequest {
    private String fullName;
    private int age;
    private String gender;
    private String strand;
    private String gradeSection;
    private String username;
    private String password;

    public RegisterRequest() {}

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

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
