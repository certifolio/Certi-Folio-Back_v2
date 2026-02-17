package com.certifolio.server.Form.Activity.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ActivityDTO {
    private Long id;
    private String name;
    private String type;
    private String role;
    private String startDate;
    private String endDate;
    private String description;
    private String result;

    public static ActivityDTO from(com.certifolio.server.Form.Activity.domain.Activity activity) {
        return ActivityDTO.builder()
                .id(activity.getId())
                .name(activity.getName())
                .type(activity.getType())
                .role(activity.getRole())
                .startDate(activity.getStartDate() != null ? activity.getStartDate().toString() : null)
                .endDate(activity.getEndDate() != null ? activity.getEndDate().toString() : null)
                .description(activity.getDescription())
                .result(activity.getResult())
                .build();
    }
}
