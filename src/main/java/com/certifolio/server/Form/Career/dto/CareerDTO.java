package com.certifolio.server.Form.Career.dto;

import com.certifolio.server.Form.Career.domain.Career;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CareerDTO {
    private Long id;
    private String company;
    private String position;
    private String department;
    private String type;
    private String startDate;
    private String endDate;
    private boolean isCurrent;
    private String location;
    private String description;
    private String skills;

    /**
     * Entity to DTO 변환
     */
    public static CareerDTO from(Career career) {
        if (career == null)
            return null;

        return CareerDTO.builder()
                .id(career.getId())
                .company(career.getCompany())
                .position(career.getPosition())
                .department(career.getDepartment())
                .type(career.getType())
                .startDate(career.getStartDate() != null ? career.getStartDate().toString() : null)
                .endDate(career.getEndDate() != null ? career.getEndDate().toString() : null)
                .isCurrent(career.isCurrent())
                .location(career.getLocation())
                .description(career.getDescription())
                .build();
    }
}
