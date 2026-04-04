package com.certifolio.server.global.apiPayload.code;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum GeneralErrorCode implements BaseErrorCode {

    // ============ COMMON (C) ============
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "COMMON_400", "잘못된 요청입니다."),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "COMMON_401", "인증이 필요합니다."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "COMMON_403", "접근 권한이 없습니다."),
    NOT_FOUND(HttpStatus.NOT_FOUND, "COMMON_404", "요청한 리소스를 찾을 수 없습니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "COMMON_405", "허용되지 않은 HTTP 메서드입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_500", "서버 내부 오류가 발생했습니다."),
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "COMMON_001", "입력값이 올바르지 않습니다."),
    INVALID_DATE_FORMAT(HttpStatus.BAD_REQUEST, "COMMON_002", "날짜 형식이 올바르지 않습니다."),
    JSON_PARSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "COMMON_003", "JSON 처리 중 오류가 발생했습니다."),

    // ============ USER (U) ============
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "해당 사용자를 찾을 수 없습니다."),
    USER_ALREADY_EXISTS(HttpStatus.CONFLICT, "USER_002", "이미 존재하는 사용자입니다."),
    INVALID_PRINCIPAL(HttpStatus.UNAUTHORIZED, "USER_003", "유효하지 않은 인증 정보입니다."),
    USER_NOT_FOUND_BY_PROVIDER(HttpStatus.NOT_FOUND, "USER_004", "OAuth 제공자 정보로 사용자를 찾을 수 없습니다."),
    USER_HAVE_NO_PREFERENCE(HttpStatus.NOT_FOUND, "USER_005", "선호 기업 유형, 직무가 없습니다."),

    // ============ AUTH (A) ============
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_001", "유효하지 않은 토큰입니다."),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_002", "만료된 토큰입니다."),
    UNSUPPORTED_PROVIDER(HttpStatus.BAD_REQUEST, "AUTH_003", "지원하지 않는 OAuth 제공자입니다."),
    OAUTH_AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "AUTH_004", "OAuth 인증에 실패했습니다."),

    // ============ FORM (F) ============
    FORM_NOT_FOUND(HttpStatus.NOT_FOUND, "FORM_001", "해당 폼 데이터를 찾을 수 없습니다."),
    FORM_UNAUTHORIZED(HttpStatus.FORBIDDEN, "FORM_002", "해당 폼에 대한 수정/삭제 권한이 없습니다."),

    // ============ ACTIVITY (AC) ============
    ACTIVITY_NOT_INPUTTED(HttpStatus.BAD_REQUEST, "ACTIVITY_001", "활동 내역을 입력하지 않았습니다."),
    ACTIVITY_NOT_FOUND(HttpStatus.NOT_FOUND, "ACTIVITY_002", "해당 활동 내역을 찾을 수 없습니다."),
    ACTIVITY_UNAUTHORIZED(HttpStatus.FORBIDDEN, "ACTIVITY_003", "해당 활동 내역에 접근 권한이 없습니다."),

    // ============ CAREER (CA) ============
    CAREER_NOT_INPUTTED(HttpStatus.BAD_REQUEST, "CAREER_001", "경력 사항을 입력하지 않았습니다."),
    CAREER_NOT_FOUND(HttpStatus.NOT_FOUND, "CAREER_002", "해당 경력 사항을 찾을 수 없습니다."),
    CAREER_UNAUTHORIZED(HttpStatus.FORBIDDEN, "CAREER_003", "해당 경력 사항에 접근 권한이 없습니다."),

    // ============ CERTIFICATE (CE) ============
    CERTIFICATE_NOT_INPUTTED(HttpStatus.BAD_REQUEST, "CERTIFICATE_001", "자격증 정보를 입력하지 않았습니다."),
    CERTIFICATE_NOT_FOUND(HttpStatus.NOT_FOUND, "CERTIFICATE_002", "해당 자격증을 찾을 수 없습니다."),
    CERTIFICATE_UNAUTHORIZED(HttpStatus.FORBIDDEN, "CERTIFICATE_003", "해당 자격증에 접근 권한이 없습니다."),

    // ============ EDUCATION (ED) ============
    EDUCATION_NOT_INPUTTED(HttpStatus.BAD_REQUEST, "EDUCATION_001", "학력 정보를 입력하지 않았습니다."),
    EDUCATION_NOT_FOUND(HttpStatus.NOT_FOUND, "EDUCATION_002", "해당 학력 정보를 찾을 수 없습니다."),
    EDUCATION_UNAUTHORIZED(HttpStatus.FORBIDDEN, "EDUCATION_003", "해당 학력 정보에 접근 권한이 없습니다."),

    // ============ ALGORITHM (AL) ============
    ALGORITHM_NOT_FOUND(HttpStatus.NOT_FOUND, "ALGORITHM_001", "등록된 알고리즘 정보가 없습니다."),
    ALGORITHM_HANDLE_NOT_FOUND(HttpStatus.NOT_FOUND, "ALGORITHM_002", "solved.ac에서 해당 핸들을 찾을 수 없습니다."),
    ALGORITHM_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "ALGORITHM_003", "solved.ac API 호출에 실패했습니다."),

    // ============ PROJECT (PJ) ============
    PROJECT_NOT_INPUTTED(HttpStatus.BAD_REQUEST, "PROJECT_001", "프로젝트 정보를 입력하지 않았습니다."),
    PROJECT_NOT_FOUND(HttpStatus.NOT_FOUND, "PROJECT_002", "해당 프로젝트를 찾을 수 없습니다."),
    PROJECT_UNAUTHORIZED(HttpStatus.FORBIDDEN, "PROJECT_003", "해당 프로젝트에 접근 권한이 없습니다."),

    // ============ MENTORING (M) ============
    MENTOR_NOT_FOUND(HttpStatus.NOT_FOUND, "MENTOR_001", "해당 멘토를 찾을 수 없습니다."),
    SELF_CHAT_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "MENTOR_002", "멘토 본인과는 채팅방을 생성할 수 없습니다."),
    MENTORING_NOT_APPROVED(HttpStatus.FORBIDDEN, "MENTOR_003", "승인된 멘토링 관계가 있어야 채팅이 가능합니다."),
    CHAT_ROOM_ACCESS_DENIED(HttpStatus.FORBIDDEN, "MENTOR_004", "해당 채팅방에 참여 권한이 없습니다."),
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "MENTOR_005", "해당 채팅방을 찾을 수 없습니다."),
    MENTOR_ALREADY_APPLIED(HttpStatus.CONFLICT, "MENTOR_006", "이미 멘토 신청을 하셨습니다."),
    MENTORING_APPLICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "MENTOR_007", "해당 멘토링 신청을 찾을 수 없습니다."),
    MENTORING_SESSION_NOT_FOUND(HttpStatus.NOT_FOUND, "MENTOR_008", "해당 멘토링 세션을 찾을 수 없습니다."),
    MENTORING_SELF_APPLICATION(HttpStatus.BAD_REQUEST, "MENTOR_009", "자기 자신에게 멘토링을 신청할 수 없습니다."),
    MENTORING_ALREADY_APPLIED(HttpStatus.CONFLICT, "MENTOR_010", "이미 대기 중인 신청이 있습니다."),
    MENTORING_APPLICATION_UNAUTHORIZED(HttpStatus.FORBIDDEN, "MENTOR_011", "본인에게 온 신청만 처리할 수 있습니다."),
    MENTORING_ALREADY_PROCESSED(HttpStatus.CONFLICT, "MENTOR_012", "이미 처리된 신청입니다."),
    MENTORING_INVALID_STATUS(HttpStatus.BAD_REQUEST, "MENTOR_013", "잘못된 세션 상태값입니다."),
    CHAT_ROOM_CREATION_FORBIDDEN(HttpStatus.FORBIDDEN, "MENTOR_014", "다른 멘토의 채팅방을 생성할 권한이 없습니다."),

    // ============ ANALYTICS (AN) ============
    ANALYTICS_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "ANALYTICS_001", "AI 분석 중 오류가 발생했습니다."),
    ANALYTICS_NOT_FOUND(HttpStatus.NOT_FOUND, "ANALYTICS_002", "분석 결과를 찾을 수 없습니다."),

    // ============ CODING TEST (CT) ============
    SOLVED_AC_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "CODING_001", "solved.ac에서 해당 사용자를 찾을 수 없습니다."),
    SOLVED_AC_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "CODING_002", "solved.ac API 호출 중 오류가 발생했습니다."),

    // ============ NOTIFICATION (N) ============
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTI_001", "해당 알림을 찾을 수 없습니다."),


    ;
    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
}
