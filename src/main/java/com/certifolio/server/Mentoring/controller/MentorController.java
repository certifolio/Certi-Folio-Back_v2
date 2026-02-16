package com.certifolio.server.Mentoring.controller;

import com.certifolio.server.User.domain.User;
import com.certifolio.server.Mentoring.dto.MentorDTO;
import com.certifolio.server.User.repository.UserRepository;
import com.certifolio.server.Mentoring.service.MentorService;
import com.certifolio.server.auth.util.AuthUtils;
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
    private final UserRepository userRepository;

    /**
     * 멘토 검색/목록 조회
     * GET /api/mentors?skills=React,Node.js&location=서울
     */
    @GetMapping
    public ResponseEntity<MentorDTO.MentorsResponse> searchMentors(
            @RequestParam(required = false) String skills,
            @RequestParam(required = false) String location) {

        List<String> skillList = skills != null ? Arrays.asList(skills.split(",")) : null;
        MentorDTO.MentorsResponse response = mentorService.searchMentors(skillList, location);
        return ResponseEntity.ok(response);
    }

    /**
     * 멘토 프로필 상세 조회
     * GET /api/mentors/{mentorId}
     */
    @GetMapping("/{mentorId}")
    public ResponseEntity<MentorDTO.MentorProfileResponse> getMentorProfile(
            @PathVariable Long mentorId) {

        MentorDTO.MentorProfileResponse response = mentorService.getMentorProfile(mentorId);
        return ResponseEntity.ok(response);
    }

    /**
     * 멘토 신청
     * POST /api/mentors/apply
     */
    @PostMapping("/apply")
    public ResponseEntity<MentorDTO.ApplyMentorResponse> applyMentor(
            @AuthenticationPrincipal Object principal,
            @RequestBody MentorDTO.MentorApplicationRequest request) {

        User user = getUser(principal);
        if (user == null) {
            return ResponseEntity.status(401).body(
                    MentorDTO.ApplyMentorResponse.builder()
                            .success(false)
                            .message("인증이 필요합니다.")
                            .build());
        }

        MentorDTO.ApplyMentorResponse response = mentorService.applyMentor(user.getId(), request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        } else {
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 내 멘토 프로필 조회
     * GET /api/mentors/me
     */
    @GetMapping("/me")
    public ResponseEntity<MentorDTO.MentorProfileResponse> getMyMentorProfile(
            @AuthenticationPrincipal Object principal) {

        User user = getUser(principal);
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        MentorDTO.MentorProfileResponse response = mentorService.getMyMentorProfile(user.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * 내 멘토 프로필 수정
     * PUT /api/mentors/me
     */
    @PutMapping("/me")
    public ResponseEntity<MentorDTO.ApplyMentorResponse> updateMyMentorProfile(
            @AuthenticationPrincipal Object principal,
            @RequestBody MentorDTO.MentorApplicationRequest request) {

        User user = getUser(principal);
        if (user == null) {
            return ResponseEntity.status(401).body(
                    MentorDTO.ApplyMentorResponse.builder()
                            .success(false)
                            .message("인증이 필요합니다.")
                            .build());
        }

        MentorDTO.ApplyMentorResponse response = mentorService.updateMentorProfile(user.getId(), request);
        return ResponseEntity.ok(response);
    }

    /**
     * Principal에서 User 조회
     */
    private User getUser(Object principal) {
        return AuthUtils.resolveUser(principal, userRepository);
    }
}
