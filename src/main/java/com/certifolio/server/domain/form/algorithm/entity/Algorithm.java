package com.certifolio.server.domain.form.algorithm.entity;

import com.certifolio.server.domain.user.entity.User;
import com.certifolio.server.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "coding_tests")
public class Algorithm extends BaseTimeEntity {

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


    @Builder
    public Algorithm(User user, String bojHandle, Integer tier, Integer solvedCount, Integer rating) {
        this.user = user;
        this.bojHandle = bojHandle;
        this.tier = tier;
        this.solvedCount = solvedCount;
        this.rating = rating;
    }

    public void update(Integer tier, Integer solvedCount, Integer rating) {
        this.tier = tier;
        this.solvedCount = solvedCount;
        this.rating = rating;
    }
}
