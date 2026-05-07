package com.certifolio.server.domain.jobposting.dto.response;

import java.util.List;

public record JobPostingCalendarResponseDTO(
        int year,
        int month,
        List<JobPostingResponseDTO> jobPostings
) {}
