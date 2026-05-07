package com.certifolio.server.domain.jobposting.entity;

import com.certifolio.server.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "job_postings")
public class JobPosting extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String companyName;

    @Column(nullable = false)
    private String state;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(nullable = false)
    private String position;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false, unique = true)
    private String link;

    @Builder
    public JobPosting(String companyName, String state, String content, String position,
                      LocalDate startDate, LocalDate endDate, String link) {
        this.companyName = companyName;
        this.state = state;
        this.content = content;
        this.position = position;
        this.startDate = startDate;
        this.endDate = endDate;
        this.link = link;
    }

    public void update(String companyName, String state, String content, String position,
                       LocalDate startDate, LocalDate endDate) {
        this.companyName = companyName;
        this.state = state;
        this.content = content;
        this.position = position;
        this.startDate = startDate;
        this.endDate = endDate;
    }
}
