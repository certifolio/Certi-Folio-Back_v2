package com.certifolio.server.Form.Activity.domain;

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
@Table(name = "activities")
public class Activity {

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
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    private String result;

    @Builder
    public Activity(User user, String name, String type, String role, LocalDate startDate, LocalDate endDate,
                    String description, String result) {
        this.user = user;
        this.name = name;
        this.type = type;
        this.role = role;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        this.result = result;
    }

    public void update(String name, String type, String role, LocalDate startDate, LocalDate endDate,
                       String description, String result) {
        this.name = name;
        this.type = type;
        this.role = role;
        this.startDate = startDate;
        this.endDate = endDate;
        this.description = description;
        this.result = result;
    }
}
