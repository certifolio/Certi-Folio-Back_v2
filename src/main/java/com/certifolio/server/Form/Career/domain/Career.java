package com.certifolio.server.Form.Career.domain;

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
@Table(name = "careers")
public class Career {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private String company;

    private String position;

    private String department;

    private String type; // intern, fulltime, etc.

    private LocalDate startDate;

    private LocalDate endDate;

    private boolean isCurrent;

    private String location; // added to match CareerItem

    @Column(columnDefinition = "TEXT")
    private String description; // Simplify list<string> to unified text for DB or use converter if needed.
    // For now assuming joined string or simple text block as per basic requirement.

    @Column(columnDefinition = "TEXT")
    private String skills; // joined string

    @Builder
    public Career(User user, String company, String position, String department, String type, LocalDate startDate,
            LocalDate endDate, boolean isCurrent, String location, String description, String skills) {
        this.user = user;
        this.company = company;
        this.position = position;
        this.department = department;
        this.type = type;
        this.startDate = startDate;
        this.endDate = endDate;
        this.isCurrent = isCurrent;
        this.location = location;
        this.description = description;
        this.skills = skills;
    }
}
