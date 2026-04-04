package com.certifolio.server.domain.mentoring.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * 멘토 가용 시간 엔티티
 */
@Entity
@Table(name = "mentor_availability")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MentorAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mentor_id", nullable = false)
    private Mentor mentor;

    @Column(nullable = false)
    private String timeSlot; // 예: "weekday-morning", "weekend-afternoon" -> 30분 단위로 선택 시간

    private String date; // 특정 날짜 (선택적) -> 필수
    private String time; // 특정 시간 (선택적) -> 필수
    private String slotType; // video, offline, chat
}
