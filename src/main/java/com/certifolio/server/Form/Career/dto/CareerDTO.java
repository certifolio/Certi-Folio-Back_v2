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
    private String type;
    private String company;
    private String position;
    private String startDate;
    private String endDate;
    private String description;

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
                .type(career.getType())
                .startDate(career.getStartDate() != null ? career.getStartDate().toString() : null)
                .endDate(career.getEndDate() != null ? career.getEndDate().toString() : null)
                .description(career.getDescription())
                .build();
    }
}
