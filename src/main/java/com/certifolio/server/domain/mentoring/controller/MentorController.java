package com.certifolio.server.domain.mentoring.controller;

import com.certifolio.server.domain.mentoring.dto.request.MentorRequestDTO;
import com.certifolio.server.domain.mentoring.dto.response.MentorResponseDTO;
import com.certifolio.server.domain.mentoring.service.MentorService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api/mentors")
@RequiredArgsConstructor
@Slf4j
public class MentorController {

    private final MentorService mentorService;

    /**
     * 멘토 검색/목록 조회
     * GET /api/mentors?skills=React,Node.js
     */
    @GetMapping
    public ResponseEntity<MentorResponseDTO.MentorsResponse> searchMentors(
            @RequestParam(required = false) String skills) {

        List<String> skillList = skills != null ? Arrays.asList(skills.split(",")) : null;
        MentorResponseDTO.MentorsResponse response = mentorService.searchMentors(skillList);
        return ResponseEntity.ok(response);
    }

    /**
     * 멘토 프로필 상세 조회
     * GET /api/mentors/{mentorId}
     */
    @GetMapping("/{mentorId}")
    public ResponseEntity<MentorResponseDTO.MentorProfileResponse> getMentorProfile(
            @PathVariable Long mentorId) {

        MentorResponseDTO.MentorProfileResponse response = mentorService.getMentorProfile(mentorId);
        return ResponseEntity.ok(response);
    }

    /**
     * 멘토 신청
     * POST /api/mentors/apply
     */
    @PostMapping("/apply")
    public ResponseEntity<MentorResponseDTO.ApplyMentorResponse> applyMentor(
            @AuthenticationPrincipal Long userId,
            @RequestBody MentorRequestDTO.MentorApplicationRequest request) {

        MentorResponseDTO.ApplyMentorResponse response = mentorService.applyMentor(userId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * 내 멘토 프로필 조회
     * GET /api/mentors/me
     */
    @GetMapping("/me")
    public ResponseEntity<MentorResponseDTO.MentorProfileResponse> getMyMentorProfile(
            @AuthenticationPrincipal Long userId) {

        MentorResponseDTO.MentorProfileResponse response = mentorService.getMyMentorProfile(userId);
        return ResponseEntity.ok(response);
    }

    /**
     * 내 멘토 프로필 수정
     * PUT /api/mentors/me
     */
    @PutMapping("/me")
    public ResponseEntity<MentorResponseDTO.ApplyMentorResponse> updateMyMentorProfile(
            @AuthenticationPrincipal Long userId,
            @RequestBody MentorRequestDTO.MentorApplicationRequest request) {

        MentorResponseDTO.ApplyMentorResponse response = mentorService.updateMentorProfile(userId, request);
        return ResponseEntity.ok(response);
    }
}
