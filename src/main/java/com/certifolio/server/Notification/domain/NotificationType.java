package com.certifolio.server.Notification.domain;

/**
 * 알림 유형
 */
public enum NotificationType {
    MENTORING,
    CERTIFICATE,
    JOB,
    STUDY,
    SYSTEM,
    ANALYSIS,
    COMMENT,
    LIKE;

    /**
     * 프론트엔드에서 사용하는 소문자 형식으로 변환
     */
    public String toFrontendValue() {
        return this.name().toLowerCase();
    }

    /**
     * 프론트엔드 문자열에서 Enum으로 변환
     */
    public static NotificationType fromFrontendValue(String value) {
        if (value == null) return null;
        try {
            return NotificationType.valueOf(value.toUpperCase());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
