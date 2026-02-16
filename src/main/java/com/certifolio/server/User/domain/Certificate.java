package com.certifolio.server.User.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "certificates")
public class Certificate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String name;

    private String issuer;

    private LocalDate issueDate;

    private LocalDate expiryDate;

    private String status; // active, expired, etc.

    private String score;

    private String certificateNumber;

    private String category;

    private String imageUrl;

    @Builder
    public Certificate(User user, String name, String issuer, LocalDate issueDate, LocalDate expiryDate, String status,
            String score, String certificateNumber, String category, String imageUrl) {
        this.user = user;
        this.name = name;
        this.issuer = issuer;
        this.issueDate = issueDate;
        this.expiryDate = expiryDate;
        this.status = status;
        this.score = score;
        this.certificateNumber = certificateNumber;
        this.category = category;
        this.imageUrl = imageUrl;
    }

    public void update(String name, String issuer, LocalDate issueDate, LocalDate expiryDate,
            String status, String score, String certificateNumber, String category, String imageUrl) {
        this.name = name;
        this.issuer = issuer;
        this.issueDate = issueDate;
        this.expiryDate = expiryDate;
        this.status = status;
        this.score = score;
        this.certificateNumber = certificateNumber;
        this.category = category;
        this.imageUrl = imageUrl;
    }
}
