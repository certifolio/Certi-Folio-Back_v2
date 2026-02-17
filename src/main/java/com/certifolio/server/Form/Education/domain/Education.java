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
    private String type; // university, highschool, etc.

    private String schoolName;

    private String major;

    private String degree;

    private String status; // enrolled, graduated, etc.

    private LocalDate startDate;

    private LocalDate endDate;

    private boolean isCurrent;

    private Double gpa;

    private Double maxGpa;

    private String location;

    @Builder
    public Education(User user, String type, String schoolName, String major, String degree, String status,
            LocalDate startDate, LocalDate endDate, boolean isCurrent, Double gpa, Double maxGpa, String location) {
        this.user = user;
        this.type = type;
        this.schoolName = schoolName;
        this.major = major;
        this.degree = degree;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isCurrent = isCurrent;
        this.gpa = gpa;
        this.maxGpa = maxGpa;
        this.location = location;
    }
}
