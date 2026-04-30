package com.certifolio.server.domain.mentoring.entity;

import com.certifolio.server.domain.user.entity.User;
import com.certifolio.server.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 멘토 엔티티
 */
@Entity
@Table(name = "mentors")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mentor extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false)
    private String title; // 직함 (예: Senior Developer)

    private String company; // 회사명

    @Column(nullable = false)
    private String experience; // 경력 (예: "5년 이상")

    @Column(columnDefinition = "TEXT")
    private String bio; // 자기소개

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PreferredFormat preferredFormat = PreferredFormat.BOTH;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MentorStatus status = MentorStatus.PENDING;

    @OneToMany(mappedBy = "mentor", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MentorSkill> skills = new ArrayList<>();

    @OneToMany(mappedBy = "mentor", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MentorAvailability> availabilities = new ArrayList<>();

    // Helper methods
    public void updateProfile(String title, String company, String experience, String bio, PreferredFormat preferredFormat) {
        this.title = title;
        this.company = company;
        this.experience = experience;
        this.bio = bio;
        this.preferredFormat = preferredFormat;
        this.skills.clear();
        this.availabilities.clear();
    }

    public void addSkill(MentorSkill skill) {
        skills.add(skill);
        skill.setMentor(this);
    }

    public void addAvailability(MentorAvailability availability) {
        availabilities.add(availability);
        availability.setMentor(this);
    }

    public String getName() {
        return user != null ? user.getName() : null;
    }
}
