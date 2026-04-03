package com.certifolio.server.domain.form.education.dto.response;

import com.certifolio.server.domain.form.education.entity.Education;
import com.certifolio.server.global.common.util.DateUtils;

public record EducationResponseDTO(
        Long id,
        String schoolName,
        String major,
        String degree,
        String status,
        String startDate,
        String endDate,
        Double gpa,
        Double maxGpa
) {
    public static EducationResponseDTO from(Education education) {
        return new EducationResponseDTO(
                education.getId(),
                education.getSchoolName(),
                education.getMajor(),
                education.getDegree(),
                education.getStatus(),
                DateUtils.dateToString(education.getStartDate()),
                DateUtils.dateToString(education.getEndDate()),
                education.getGpa(),
                education.getMaxGpa()
        );
    }
}
