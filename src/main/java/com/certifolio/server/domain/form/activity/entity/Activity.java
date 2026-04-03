package com.certifolio.server.domain.form.activity.entity;

import com.certifolio.server.domain.user.entity.User;
import com.certifolio.server.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "activities")
public class Activity extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String type;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private LocalDate startMonth;

    @Column(nullable = false)
    private LocalDate endMonth;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String result;

    @Builder
    public Activity(User user, String name, String type, String role, LocalDate startMonth, LocalDate endMonth,
                    String description, String result) {
        this.user = user;
        this.name = name;
        this.type = type;
        this.role = role;
        this.startMonth = startMonth;
        this.endMonth = endMonth;
        this.description = description;
        this.result = result;
    }

    public void update(String name, String type, String role, LocalDate startDate, LocalDate endDate,
                       String description, String result) {
        this.name = name;
        this.type = type;
        this.role = role;
        this.startMonth = startDate;
        this.endMonth = endDate;
        this.description = description;
        this.result = result;
    }
}
