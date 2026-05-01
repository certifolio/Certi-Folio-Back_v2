package com.certifolio.server.domain.user.entity;

import com.certifolio.server.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users")
public class User extends BaseTimeEntity {

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

    private Integer birthYear;

    @Builder
    public User(String name, String email, String picture, Role role, String provider, String providerId) {
        this.name = name;
        this.email = email;
        this.picture = picture;
        this.role = role;
        this.provider = provider;
        this.providerId = providerId;
    }

    public void update(String name, Integer birthYear) {
        if (name != null) this.name = name;
        if (birthYear != null) this.birthYear = birthYear;
    }

    public void updatePicture(String picture) {
        this.picture = picture;
    }
}
