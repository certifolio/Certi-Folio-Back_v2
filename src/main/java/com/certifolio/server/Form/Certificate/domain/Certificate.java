package com.certifolio.server.Form.Certificate.domain;

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

    @Column(nullable = false)
    private String type; // 어학 자격증, 일반 자격증

    private String issuer; // 일반 자격증에만 추가

    @Column(nullable = false)
    private LocalDate issueDate;

    private LocalDate expiryDate;

    @Column(nullable = false)
    private String score;

    private String certificateNumber;

    @Builder
    public Certificate(User user, String name, String type, String issuer, LocalDate issueDate,
                       String score, String certificateNumber) {
        this.user = user;
        this.name = name;
        this.type = type;
        this.issuer = issuer;
        this.issueDate = issueDate;
        this.score = score;
        this.certificateNumber = certificateNumber;
        this.expiryDate = calculateExpiryDate(name, type, issueDate);
    }

    public void update(String name, String type, String issuer, LocalDate issueDate,
                       String score, String certificateNumber) {
        this.name = name;
        this.type = type;
        this.issuer = issuer;
        this.issueDate = issueDate;
        this.score = score;
        this.certificateNumber = certificateNumber;
        this.expiryDate = calculateExpiryDate(name, type, issueDate);
    }

    private LocalDate calculateExpiryDate(String certName, String type, LocalDate issueDate) {
        if (issueDate == null) {
            return null;
        }

        // 2. 어학 자격증은 보통 2년
        if ("language".equals(type)) {
            return issueDate.plusYears(2);
        }

        // 3. 자격증명 기반 계산
        if (certName != null) {
            String lower = certName.toLowerCase();

            // 어학 시험 (2년)
            if (lower.contains("toeic") || lower.contains("토익") ||
                    lower.contains("toefl") || lower.contains("토플") ||
                    lower.contains("opic") || lower.contains("오픽") ||
                    lower.contains("teps") || lower.contains("텝스")) {
                return issueDate.plusYears(2);
            }

            // 기술 자격증 (일부 갱신 필요)
            if (lower.contains("aws") || lower.contains("gcp") || lower.contains("azure")) {
                return issueDate.plusYears(3);  // 클라우드 자격증 3년
            }
        }

        // 4. 일반 자격증은 평생 유효 (null)
        return null;
    }
}
