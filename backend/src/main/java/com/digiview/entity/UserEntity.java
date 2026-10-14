package com.digiview.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "full_name", nullable = false)
    private String fullName;

    @Column(nullable = false)
    private int age = 16;

    @Column(nullable = false)
    private String gender;

    @Column(nullable = false)
    private String strand;

    @Column(name = "grade_section", nullable = false)
    private String gradeSection;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private int xp = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public UserEntity() {}

    public UserEntity(String fullName, int age, String gender, String strand, String gradeSection, String username, String password) {
        this.fullName = fullName;
        this.age = age;
        this.gender = gender;
        this.strand = strand;
        this.gradeSection = gradeSection;
        this.username = username;
        this.password = password;
        this.xp = 0;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

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

    public int getXp() { return xp; }
    public void setXp(int xp) { this.xp = xp; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
