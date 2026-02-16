package com.certifolio.server.Activity.domain;

import com.certifolio.server.User.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

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

    private String type;

    private String organizer;

    private String role;

    private String period; // e.g., "2023.01 - 2023.06"

    @Column(columnDefinition = "TEXT")
    private String description;

    private String link;

    private String result;

    @Builder
    public Activity(User user, String name, String type, String organizer, String role, String period,
            String description, String link, String result) {
        this.user = user;
        this.name = name;
        this.type = type;
        this.organizer = organizer;
        this.role = role;
        this.period = period;
        this.description = description;
        this.link = link;
        this.result = result;
    }

    public void update(String name, String type, String organizer, String role,
            String period, String description, String link, String result) {
        this.name = name;
        this.type = type;
        this.organizer = organizer;
        this.role = role;
        this.period = period;
        this.description = description;
        this.link = link;
        this.result = result;
    }
}
