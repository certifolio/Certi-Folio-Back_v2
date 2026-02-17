package com.certifolio.server.User.domain;

// Role is now in the same package, no import needed

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "users")
public class User {
        

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @Column(unique = true)
    private String nickname;

    @Column
    private String email;

    @Column
    private String picture;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column
    private String provider; // google, naver, kakao

    @Column
    private String providerId;

    // Additional profile fields
    @Column
    private String phone;

    @Column
    private String location;

    @Column
    private String university;

    @Column
    private String major;

    @Column(name = "student_year")
    private String year;

    @Column
    private String company;

    @Column(length = 500)
    private String bio;

    @Column(nullable = false)
    private boolean isInfoInputted = false; // Default false

    @Column(nullable = false)
    private boolean isAdmin = false; // Default false

    @Builder
    public User(String name, String email, String picture, Role role, String provider, String providerId,
            String nickname) {
        this.name = name;
        this.email = email;
        this.picture = picture;
        this.role = role;
        this.provider = provider;
        this.providerId = providerId;
        this.nickname = nickname;
    }

    public User update(String name, String picture) {
        this.name = name;
        this.picture = picture;
        return this;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    /**
     * Update profile fields
     */
    public void updateProfile(String nickname, String phone, String location,
            String university, String major, String year,
            String company, String bio) {
        if (nickname != null)
            this.nickname = nickname;
        if (phone != null)
            this.phone = phone;
        if (location != null)
            this.location = location;
        if (university != null)
            this.university = university;
        if (major != null)
            this.major = major;
        if (year != null)
            this.year = year;
        if (company != null)
            this.company = company;
        if (bio != null)
            this.bio = bio;
    }

    public void updateBasicInfo(String name, boolean isInfoInputted) {
        if (name != null) this.name = name;
        this.isInfoInputted = isInfoInputted;
    }

    public String getRoleKey() {
        return this.role.getKey();
    }
}
