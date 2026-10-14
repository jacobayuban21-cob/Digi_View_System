package com.digiview.lms.model;

import java.io.Serializable;

public class User implements Serializable {
    public Long id;
    public String fullName;
    public int age;
    public String gender;
    public String strand;
    public String gradeSection;
    public String username;
    public int xp;

    public User() {}
}
