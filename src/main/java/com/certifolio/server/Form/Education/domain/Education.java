package com.certifolio.server.Form.Education.domain;

import com.certifolio.server.User.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "educations")
public class Education {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String schoolName;

    @Column(nullable = false)
    private String major;

    @Column(nullable = false)
    private String degree; // 학사, 전문학사, 석사, 박사

    @Column(nullable = false)
    private String status; // 재학, 휴학, 졸업, 졸업예정

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    private Double gpa;

    private Double maxGpa;

    @Builder
    public Education(User user, String schoolName, String major, String degree, String status,
                     LocalDate startDate, LocalDate endDate, Double gpa, Double maxGpa) {
        this.user = user;
        this.schoolName = schoolName;
        this.major = major;
        this.degree = degree;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.gpa = gpa;
        this.maxGpa = maxGpa;
    }

    public void update(String schoolName, String major, String degree, String status, LocalDate startDate,
                       LocalDate endDate, Double gpa, Double maxGpa) {
        this.schoolName = schoolName;
        this.major = major;
        this.degree = degree;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.gpa = gpa;
        this.maxGpa = maxGpa;
    }
}
