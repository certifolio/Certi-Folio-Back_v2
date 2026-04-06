package com.certifolio.server.domain.notification.scheduler;

import com.certifolio.server.domain.form.certificate.entity.Certificate;
import com.certifolio.server.domain.form.certificate.repository.CertificateRepository;
import com.certifolio.server.domain.notification.entity.NotificationType;
import com.certifolio.server.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class CertificateNotificationScheduler {

    private final CertificateRepository certificateRepository;
    private final NotificationService notificationService;

    // 매일 오전 9시 실행 — 만료 2개월 전 자격증 알림
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional
    public void notifyExpiringCertificates() {
        log.info("자격증 만료 알림 스케줄러 시작");

        LocalDate targetExpiryDate = LocalDate.now().plusMonths(2);
        List<Certificate> expiringCertificates = certificateRepository.findByExpiryDate(targetExpiryDate);

        for (Certificate cert : expiringCertificates) {
            try {
                notificationService.createNotification(
                        cert.getUser(),
                        NotificationType.CERTIFICATE,
                        "자격증 만료 예정 알림",
                        String.format("'%s' 자격증이 2개월 후 만료됩니다. 갱신을 준비하세요.", cert.getName()),
                        "/portfolio"
                );
                log.info("알림 발송: userId={}, certificate={}", cert.getUser().getId(), cert.getName());
            } catch (Exception e) {
                log.error("알림 발송 실패: certificateId={}, error={}", cert.getId(), e.getMessage());
            }
        }

        log.info("자격증 만료 알림 스케줄러 종료. 처리 건수: {}", expiringCertificates.size());
    }
}
