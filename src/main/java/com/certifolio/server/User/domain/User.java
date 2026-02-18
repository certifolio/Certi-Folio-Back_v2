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

    @Column(nullable = false)
    private String name;

    @Column
    private String email;

    @Column
    private String picture;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private String provider; // google, naver, kakao

    @Column(nullable = false)
    private String providerId;

    // Changed from year (String) to birthYear (Integer)
    @Column(name = "birth_year")
    private Integer birthYear;

    @Column(nullable = false)
    private boolean isInfoInputted = false; // Default false

    @Builder
    public User(String name, String email, String picture, Role role, String provider, String providerId) {
        this.name = name;
        this.email = email;
        this.picture = picture;
        this.role = role;
        this.provider = provider;
        this.providerId = providerId;
    }

    public User update(String name, String picture) {
        this.name = name;
        this.picture = picture;
        return this;
    }

    public void updateBasicInfo(String name, Integer birthYear, boolean isInfoInputted) {
        if (name != null) this.name = name;
        if (birthYear != null) this.birthYear = birthYear;
        this.isInfoInputted = isInfoInputted;
    }
    
    // Admin role handling is now done via Role enum
    public void upgradeToAdmin() {
        this.role = Role.ADMIN;
    }

    public String getRoleKey() {
        return this.role.getKey();
    }
}
