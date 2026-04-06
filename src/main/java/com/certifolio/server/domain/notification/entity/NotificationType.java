package com.certifolio.server.domain.notification.entity;

public enum NotificationType {
    JOB,
    MENTORING,
    SYSTEM,
    CERTIFICATE;

    public String toFrontendValue() {
        return this.name().toLowerCase();
    }
}
