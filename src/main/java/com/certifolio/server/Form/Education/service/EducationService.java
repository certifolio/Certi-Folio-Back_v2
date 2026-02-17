package com.certifolio.server.Form.Education.service;

import com.certifolio.server.User.domain.User;
import com.certifolio.server.User.repository.UserRepository;
import com.certifolio.server.Form.Education.domain.Education;
import com.certifolio.server.Form.Education.dto.EducationDTO;
import com.certifolio.server.Form.util.DateUtils;
import com.certifolio.server.Form.Education.repository.EducationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class EducationService {

    private final UserRepository userRepository;
    private final EducationRepository educationRepository;

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    /**
     * 학력 정보 조회 (단건)
     */
    public EducationDTO getEducation(Long userId) {
        User user = getUser(userId);
        return educationRepository.findByUser(user)
                .map(EducationDTO::from)
                .orElse(null); // 없으면 null 반환
    }

    /**
     * 학력 정보 저장 (Upsert: 있으면 수정, 없으면 생성)
     */
    public EducationDTO saveEducation(Long userId, EducationDTO dto) {
        User user = getUser(userId);

        // 기존 학력 정보 조회
        Education education = educationRepository.findByUser(user)
                .orElse(null);

        if (education == null) {
            // 없으면 생성 (Create)
            education = Education.builder()
                    .user(user)
                    .schoolName(dto.getSchoolName())
                    .major(dto.getMajor())
                    .degree(dto.getDegree())
                    .status(dto.getStatus())
                    .startDate(DateUtils.parseDate(dto.getStartDate()))
                    .endDate(DateUtils.parseDate(dto.getEndDate()))
                    .gpa(dto.getGpa())
                    .maxGpa(dto.getMaxGpa())
                    .build();
        } else {
            // 있으면 수정 (Update)
            education.update(
                    dto.getSchoolName(),
                    dto.getMajor(),
                    dto.getDegree(),
                    dto.getStatus(),
                    DateUtils.parseDate(dto.getStartDate()),
                    DateUtils.parseDate(dto.getEndDate()),
                    dto.getGpa(),
                    dto.getMaxGpa()
            );
        }

        Education saved = educationRepository.save(education);
        return EducationDTO.from(saved);
    }
}
