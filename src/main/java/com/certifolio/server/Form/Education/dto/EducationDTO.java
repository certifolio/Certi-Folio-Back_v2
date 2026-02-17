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
    private String type;
    private String schoolName;
    private String major;
    private String degree;
    private String status;
    private String startDate;
    private String endDate;
    private boolean isCurrent;
    private Double gpa;
    private Double maxGpa;
    private String location;

    /**
     * Entity to DTO 변환
     */
    public static EducationDTO from(Education education) {
        if (education == null)
            return null;

        return EducationDTO.builder()
                .id(education.getId())
                .type(education.getType())
                .schoolName(education.getSchoolName())
                .major(education.getMajor())
                .degree(education.getDegree())
                .status(education.getStatus())
                .startDate(education.getStartDate() != null ? education.getStartDate().toString() : null)
                .endDate(education.getEndDate() != null ? education.getEndDate().toString() : null)
                .isCurrent(education.isCurrent())
                .gpa(education.getGpa())
                .maxGpa(education.getMaxGpa())
                .location(education.getLocation())
                .build();
    }
}
