package com.certifolio.server.Notification.scheduler;

import com.certifolio.server.Notification.domain.NotificationType;
import com.certifolio.server.Notification.service.NotificationService;
import com.certifolio.server.Form.Certificate.domain.Certificate;
import com.certifolio.server.Form.Certificate.repository.CertificateRepository;
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

    /**
     * 자격증 만료 알림 스케줄러 (매일 오전 9시 실행)
     * 만료 2개월 전 알림 발송
     */
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional
    public void notifyExpiringCertificates() {
        log.info("자격증 만료 알림 스케줄러 시작");

        LocalDate today = LocalDate.now();
        LocalDate targetExpiryDate = today.plusMonths(2);

        // 1. 만료일이 명시된 자격증 중, 2개월 후 만료되는 것 조회
        List<Certificate> expiringCertificates = certificateRepository.findByExpiryDate(targetExpiryDate);

        // 2. 만료일이 없고(null), 발급일로부터 2년(유효기간 가정) - 2개월 전인 자격증 조회
        // 즉, 발급일이 (오늘 + 2개월 - 2년)인 자격증
        LocalDate targetIssueDate = targetExpiryDate.minusYears(2);
        List<Certificate> implicitExpiringCertificates = certificateRepository
                .findByExpiryDateIsNullAndIssueDate(targetIssueDate);

        // 병합 처리
        processNotifications(expiringCertificates);
        processNotifications(implicitExpiringCertificates);

        log.info("자격증 만료 알림 스케줄러 종료. 처리 건수: {}",
                expiringCertificates.size() + implicitExpiringCertificates.size());
    }

    private void processNotifications(List<Certificate> certificates) {
        for (Certificate cert : certificates) {
            try {
                String title = "자격증 만료 예정 알림";
                String message = String.format("'%s' 자격증이 2개월 후 만료됩니다. 갱신을 준비하세요.", cert.getName());

                // 알림 생성
                notificationService.createNotification(
                        cert.getUser(),
                        NotificationType.CERTIFICATE,
                        title,
                        message,
                        "/portfolio" // 포트폴리오 페이지로 이동
                );

                log.info("알림 발송: user={}, certificate={}", cert.getUser().getId(), cert.getName());
            } catch (Exception e) {
                log.error("알림 발송 실패: certificateId={}, error={}", cert.getId(), e.getMessage());
            }
        }
    }
}
