package com.certifolio.server.domain.jobposting.dto.response;

import com.certifolio.server.domain.jobposting.entity.JobPosting;

import java.time.LocalDate;

public record JobPostingResponseDTO(
        Long id,
        String companyName,
        String state,
        String content,
        String position,
        LocalDate startDate,
        LocalDate endDate,
        String link
) {
    public static JobPostingResponseDTO from(JobPosting jobPosting) {
        return new JobPostingResponseDTO(
                jobPosting.getId(),
                jobPosting.getCompanyName(),
                jobPosting.getState(),
                jobPosting.getContent(),
                jobPosting.getPosition(),
                jobPosting.getStartDate(),
                jobPosting.getEndDate(),
                jobPosting.getLink()
        );
    }
}
