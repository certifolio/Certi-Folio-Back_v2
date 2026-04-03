package com.certifolio.server.domain.form.education.service;

import com.certifolio.server.domain.form.education.dto.request.EducationRequestDTO;
import com.certifolio.server.domain.form.education.dto.response.EducationResponseDTO;
import com.certifolio.server.domain.form.education.entity.Education;
import com.certifolio.server.domain.form.education.repository.EducationRepository;
import com.certifolio.server.domain.user.entity.User;
import com.certifolio.server.domain.user.service.UserService;
import com.certifolio.server.global.apiPayload.code.GeneralErrorCode;
import com.certifolio.server.global.apiPayload.exception.BusinessException;
import com.certifolio.server.global.common.util.DateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EducationService {

    private final UserService userService;
    private final EducationRepository educationRepository;

    // 학력 전체 조회
    @Transactional(readOnly = true)
    public List<EducationResponseDTO> getEducations(Long userId) {
        return educationRepository.findAllByUserId(userId).stream()
                .map(EducationResponseDTO::from)
                .toList();
    }

    // 학력 전체 저장 (최초)
    @Transactional
    public void saveEducation(Long userId, List<EducationRequestDTO> request) {
        User user = userService.getUserById(userId);

        if (request == null || request.isEmpty()) {
            throw new BusinessException(GeneralErrorCode.EDUCATION_NOT_INPUTTED);
        }

        List<Education> educations = request.stream()
                .map(dto -> Education.builder()
                        .user(user)
                        .schoolName(dto.schoolName())
                        .major(dto.major())
                        .degree(dto.degree())
                        .status(dto.status())
                        .startDate(DateUtils.parseDate(dto.startDate()))
                        .endDate(DateUtils.parseDate(dto.endDate()))
                        .gpa(dto.gpa())
                        .maxGpa(dto.maxGpa())
                        .build())
                .toList();

        educationRepository.saveAll(educations);
    }

    // 학력 단건 조회
    @Transactional(readOnly = true)
    public EducationResponseDTO getEducation(Long userId, Long educationId) {
        Education education = getEducationWithOwnerCheck(userId, educationId);
        return EducationResponseDTO.from(education);
    }

    // 학력 단건 추가
    @Transactional
    public EducationResponseDTO addEducation(Long userId, EducationRequestDTO request) {
        User user = userService.getUserById(userId);
        Education education = Education.builder()
                .user(user)
                .schoolName(request.schoolName())
                .major(request.major())
                .degree(request.degree())
                .status(request.status())
                .startDate(DateUtils.parseDate(request.startDate()))
                .endDate(DateUtils.parseDate(request.endDate()))
                .gpa(request.gpa())
                .maxGpa(request.maxGpa())
                .build();
        return EducationResponseDTO.from(educationRepository.save(education));
    }

    // 학력 단건 수정
    @Transactional
    public EducationResponseDTO modifyEducation(Long userId, Long educationId, EducationRequestDTO request) {
        Education education = getEducationWithOwnerCheck(userId, educationId);
        education.update(
                request.schoolName(),
                request.major(),
                request.degree(),
                request.status(),
                DateUtils.parseDate(request.startDate()),
                DateUtils.parseDate(request.endDate()),
                request.gpa(),
                request.maxGpa()
        );
        return EducationResponseDTO.from(education);
    }

    // 학력 단건 삭제
    @Transactional
    public void deleteEducation(Long userId, Long educationId) {
        Education education = getEducationWithOwnerCheck(userId, educationId);
        educationRepository.delete(education);
    }

    // 학력 확인
    private Education getEducationWithOwnerCheck(Long userId, Long educationId) {
        Education education = educationRepository.findById(educationId)
                .orElseThrow(() -> new BusinessException(GeneralErrorCode.EDUCATION_NOT_FOUND));
        if (!education.getUser().getId().equals(userId)) {
            throw new BusinessException(GeneralErrorCode.EDUCATION_UNAUTHORIZED);
        }
        return education;
    }
}
