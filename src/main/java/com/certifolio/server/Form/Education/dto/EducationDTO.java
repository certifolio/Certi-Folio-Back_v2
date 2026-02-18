package com.certifolio.server.Form.Education.dto;

import com.certifolio.server.Form.Education.domain.Education;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EducationDTO {
    private Long id;
    private String schoolName;
    private String major;
    private String degree;
    private String status;
    private String startDate;
    private String endDate;
    private Double gpa;
    private Double maxGpa;

    /**
     * Entity to DTO 변환
     */
    public static EducationDTO from(Education education) {
        if (education == null)
            return null;

        return EducationDTO.builder()
                .id(education.getId())
                .schoolName(education.getSchoolName())
                .major(education.getMajor())
                .degree(education.getDegree())
                .status(education.getStatus())
                .startDate(education.getStartDate() != null ? education.getStartDate().toString() : null)
                .endDate(education.getEndDate() != null ? education.getEndDate().toString() : null)
                .gpa(education.getGpa())
                .maxGpa(education.getMaxGpa())
                .build();
    }
}
