package com.certifolio.server.domain.mentoring.entity;

/**
 * 멘토링 신청 상태
 */
public enum ApplicationStatus {
    PENDING, // 대기 중
    APPROVED, // 승인됨
    REJECTED // 거절됨
}
