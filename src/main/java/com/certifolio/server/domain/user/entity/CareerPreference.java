package com.certifolio.server.domain.user.entity;

import com.certifolio.server.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "career_preferences")
public class CareerPreference extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String jobRole;

    @Column(nullable = false)
    private String companyType;

    @Builder
    public CareerPreference(User user, String jobRole, String companyType) {
        this.user = user;
        this.jobRole = jobRole;
        this.companyType = companyType;
    }

    public void update(String companyType, String jobRole) {
        this.companyType = companyType;
        this.jobRole = jobRole;
    }
}

