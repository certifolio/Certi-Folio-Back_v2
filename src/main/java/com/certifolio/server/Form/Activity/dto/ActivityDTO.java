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
    private String organizer;
    private String role;
    private String period;
    private String description;
    private String link;
    private String result;
}
