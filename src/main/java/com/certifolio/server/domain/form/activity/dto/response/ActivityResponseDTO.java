package com.certifolio.server.domain.form.activity.dto.response;

import com.certifolio.server.domain.form.activity.entity.Activity;
import com.certifolio.server.global.common.util.DateUtils;

public record ActivityResponseDTO(
        Long id,
        String name,
        String type,
        String role,
        String startMonth,
        String endMonth,
        String description,
        String result
) {
    public static ActivityResponseDTO from(Activity activity) {
        return new ActivityResponseDTO(
                activity.getId(),
                activity.getName(),
                activity.getType(),
                activity.getRole(),
                DateUtils.dateToString(activity.getStartMonth()),
                DateUtils.dateToString(activity.getEndMonth()),
                activity.getDescription(),
                activity.getResult()
        );
    }
}
