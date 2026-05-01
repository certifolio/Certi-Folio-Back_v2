package com.certifolio.server.domain.portfolio.entity;

import com.certifolio.server.domain.user.entity.User;
import com.certifolio.server.global.common.entity.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@Table(name = "portfolio_drafts")
public class PortfolioDraft extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DraftStatus status;

    @Lob
    @Column(columnDefinition = "LONGTEXT", nullable = false)
    private String draftContent;

    @Builder
    public PortfolioDraft(User user, DraftStatus status, String draftContent) {
        this.user = user;
        this.status = status;
        this.draftContent = draftContent;
    }

    public void update(String draftContent) {
        this.draftContent = draftContent;
        this.status = DraftStatus.EDITED;
    }
}
