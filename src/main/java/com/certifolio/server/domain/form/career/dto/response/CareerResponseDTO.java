package com.certifolio.server.domain.form.career.dto.response;

import com.certifolio.server.domain.form.career.entity.Career;
import com.certifolio.server.global.common.util.DateUtils;

public record CareerResponseDTO(
        Long id,
        String type,
        String company,
        String position,
        String startDate,
        String endDate,
        String description
) {
    public static CareerResponseDTO from(Career career) {
        return new CareerResponseDTO(
                career.getId(),
                career.getType(),
                career.getCompany(),
                career.getPosition(),
                DateUtils.dateToString(career.getStartDate()),
                DateUtils.dateToString(career.getEndDate()),
                career.getDescription()
        );
    }
}
