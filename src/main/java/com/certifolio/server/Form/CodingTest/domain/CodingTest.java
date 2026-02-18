package com.certifolio.server.Form.CodingTest.domain;

import com.certifolio.server.User.domain.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "coding_tests")
public class CodingTest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private String bojHandle;

    private Integer tier;
    private Integer solvedCount;
    private Integer rating;
    private Integer maxStreak;
    private Integer rank;
    private String bio;
    
    // Coding class (e.g., "c", "c++", "java", etc.) - optional specific
    @Column(name = "user_class")
    private Integer userClass;

    @Builder
    public CodingTest(User user, String bojHandle, Integer tier, Integer solvedCount, Integer rating, Integer maxStreak, Integer rank, String bio, Integer userClass) {
        this.user = user;
        this.bojHandle = bojHandle;
        this.tier = tier;
        this.solvedCount = solvedCount;
        this.rating = rating;
        this.maxStreak = maxStreak;
        this.rank = rank;
        this.bio = bio;
        this.userClass = userClass;
    }
    
    public void update(Integer tier, Integer solvedCount, Integer rating, Integer maxStreak, Integer rank, String bio, Integer userClass) {
        this.tier = tier;
        this.solvedCount = solvedCount;
        this.rating = rating;
        this.maxStreak = maxStreak;
        this.rank = rank;
        this.bio = bio;
        this.userClass = userClass;
    }
}
