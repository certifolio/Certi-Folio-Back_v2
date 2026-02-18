package com.certifolio.server.Mentoring.domain;

import jakarta.persistence.*;
import lombok.*;

/**
 * 멘토 스킬/전문 분야 엔티티 (통합시키기)
 */
@Entity
@Table(name = "mentor_skills")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    private Mentor mentor;

    @Column(nullable = false)
    private String skillName; // 스킬명 (예: "백엔드 개발", "React")

    private Integer level; // 숙련도 (1-5)
}
