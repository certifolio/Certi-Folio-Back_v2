package com.certifolio.server.Form.Career.domain;

import com.certifolio.server.User.domain.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "careers")
public class Career {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String type; // 인턴쉽, 정규/계약직

    @Column(nullable = false)
    private String company;

    private String position; // 정규/계약직에만 존재 (nullable)

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description; // Simplify list<string> to unified text for DB or use converter if needed.
    // For now assuming joined string or simple text block as per basic requirement.

    @Builder
    public Career(User user, String type, String company, String position, LocalDate startDate,
                  LocalDate endDate, String description) {
        this.user = user;
        this.type = type;
        this.company = company;
        this.position = position;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
    }

    public void update(String type, String company, String position, LocalDate startDate,
                       LocalDate endDate, String description) {
        this.type = type;
        this.company = company;
        this.position = position;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
    }
}
