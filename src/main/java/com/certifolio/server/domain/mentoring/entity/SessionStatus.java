package com.certifolio.server.domain.mentoring.entity;

/**
 * 멘토링 세션 상태
 */
public enum SessionStatus {
    PENDING, // 대기 중
    ACTIVE, // 진행 중
    SCHEDULED, // 예정됨
    COMPLETED, // 완료
    CANCELLED // 취소
}
