package com.certifolio.server.Mentoring.domain;

import com.certifolio.server.User.domain.User;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 멘토 엔티티
 */
@Entity
@Table(name = "mentors")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Mentor {

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
    private String preferredFormat = "both"; // 선호 형식 (online/offline/both)

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

    @Column(nullable = false)
    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Helper methods
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
