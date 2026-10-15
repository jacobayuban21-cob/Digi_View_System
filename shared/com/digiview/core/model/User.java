package com.digiview.core.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Universal User Account Blueprint shared by Desktop Swing and Android Mobile.
 */
public class User implements Serializable {
    private static final long serialVersionUID = 1L;

    public String fullName;
    public int age;
    public String gender;
    public String strand;
    public String gradeSection;
    public String username;
    public String password;
    public int xp;

    public List<QuizRecord> history = new ArrayList<>();
    public Set<String> achievements = new LinkedHashSet<>();

    public User() {
    }

    public User(String fullName, int age, String gender, String strand, String gradeSection, String username, String password) {
        this(fullName, age, gender, strand, gradeSection, username, password, 0);
    }

    public User(String fullName, int age, String gender, String strand, String gradeSection, String username, String password, int xp) {
        this.fullName = fullName != null ? fullName : "";
        this.age = age;
        this.gender = gender != null ? gender : "";
        this.strand = strand != null ? strand : "";
        this.gradeSection = gradeSection != null ? gradeSection : "";
        this.username = username != null ? username : "";
        this.password = password != null ? password : "";
        this.xp = xp;
    }
}
