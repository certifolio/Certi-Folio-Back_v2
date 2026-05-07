package com.certifolio.server.domain.mentoring.controller;

import com.certifolio.server.domain.mentoring.dto.request.MentorRequestDTO;
import com.certifolio.server.domain.mentoring.dto.response.MentorResponseDTO;
import com.certifolio.server.domain.mentoring.entity.MentorStatus;
import com.certifolio.server.domain.mentoring.service.MentorService;
import com.certifolio.server.global.apiPayload.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/mentors")
@RequiredArgsConstructor
@Slf4j
public class AdminMentorController {

    private final MentorService mentorService;

    /**
     * 멘토 신청 목록 조회
     * GET /api/admin/mentors?status=PENDING
     * status: PENDING | APPROVED | REJECTED | (생략 시 전체)
     */
    @GetMapping
    public ApiResponse<MentorResponseDTO.AdminMentorListResponse> getMentorList(
            @RequestParam(required = false) String status) {

        MentorStatus mentorStatus = null;
        if (status != null && !status.isBlank()) {
            try {
                mentorStatus = MentorStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // 잘못된 status 값은 무시하고 전체 조회
            }
        }

        MentorResponseDTO.AdminMentorListResponse response = mentorService.getAdminMentorList(mentorStatus);
        return ApiResponse.onSuccess("멘토 목록 조회 성공", response);
    }

    /**
     * 멘토 신청 승인
     * PATCH /api/admin/mentors/{mentorId}/approve
     */
    @PatchMapping("/{mentorId}/approve")
    public ApiResponse<MentorResponseDTO.AdminMentorActionResponse> approveMentor(
            @PathVariable Long mentorId) {

        log.info("관리자 멘토 승인 요청: mentorId={}", mentorId);
        MentorResponseDTO.AdminMentorActionResponse response = mentorService.approveMentor(mentorId);
        return ApiResponse.onSuccess("멘토 신청이 승인되었습니다.", response);
    }

    /**
     * 멘토 신청 거절
     * PATCH /api/admin/mentors/{mentorId}/reject
     */
    @PatchMapping("/{mentorId}/reject")
    public ApiResponse<MentorResponseDTO.AdminMentorActionResponse> rejectMentor(
            @PathVariable Long mentorId,
            @RequestBody(required = false) MentorRequestDTO.RejectRequest request) {

        String reason = (request != null) ? request.reason() : null;
        log.info("관리자 멘토 거절 요청: mentorId={}, reason={}", mentorId, reason);
        MentorResponseDTO.AdminMentorActionResponse response = mentorService.rejectMentor(mentorId, reason);
        return ApiResponse.onSuccess("멘토 신청이 거절되었습니다.", response);
    }
}
