package com.digiview.lms.model;

public class RegisterRequest {
    public String fullName;
    public int age;
    public String gender;
    public String strand;
    public String gradeSection;
    public String username;
    public String password;

    public RegisterRequest(String fullName, int age, String gender, String strand, String gradeSection, String username, String password) {
        this.fullName = fullName;
        this.age = age;
        this.gender = gender;
        this.strand = strand;
        this.gradeSection = gradeSection;
        this.username = username;
        this.password = password;
    }
}
