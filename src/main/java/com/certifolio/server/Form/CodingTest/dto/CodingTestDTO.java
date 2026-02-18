package com.certifolio.server.Form.CodingTest.dto;

import com.certifolio.server.Form.CodingTest.domain.CodingTest;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CodingTestDTO {
    private Long id;
    private String bojHandle;
    private Integer tier;
    private Integer solvedCount;
    private Integer rating;
    private Integer maxStreak;
    private Integer rank;
    private String bio;
    private Integer userClass;
    
    // For solved.ac response mapping
    private String profileImageUrl;

    public static CodingTestDTO from(CodingTest codingTest) {
        return CodingTestDTO.builder()
                .id(codingTest.getId())
                .bojHandle(codingTest.getBojHandle())
                .tier(codingTest.getTier())
                .solvedCount(codingTest.getSolvedCount())
                .rating(codingTest.getRating())
                .maxStreak(codingTest.getMaxStreak())
                .rank(codingTest.getRank())
                .bio(codingTest.getBio())
                .userClass(codingTest.getUserClass())
                .build();
    }
}
