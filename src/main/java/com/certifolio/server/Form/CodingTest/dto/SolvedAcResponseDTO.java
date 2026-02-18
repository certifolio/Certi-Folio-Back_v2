package com.certifolio.server.Form.CodingTest.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class SolvedAcResponseDTO {
    private String handle;
    private String bio;
    private Integer tier;
    private Integer solvedCount;
    private Integer rating;
    private Integer maxStreak;
    private Integer rank;
    private String profileImageUrl;
    
    @JsonProperty("class")
    private Integer userClass;
}
