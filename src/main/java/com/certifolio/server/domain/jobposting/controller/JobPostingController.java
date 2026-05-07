package com.certifolio.server.domain.jobposting.controller;

import com.certifolio.server.domain.jobposting.dto.response.JobPostingCalendarResponseDTO;
import com.certifolio.server.domain.jobposting.service.JobPostingService;
import com.certifolio.server.global.apiPayload.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/job-postings")
@RequiredArgsConstructor
public class JobPostingController {

    private final JobPostingService jobPostingService;

    @PostMapping("/import")
    public ApiResponse<String> importJobPostings(
            @RequestBody Map<String, Map<String, String>> data) {
        int count = jobPostingService.importJobPostings(data);
        return ApiResponse.onSuccess(count + "개의 채용공고가 저장되었습니다.", count + "건 저장");
    }

    @GetMapping("/calendar")
    public ApiResponse<JobPostingCalendarResponseDTO> getCalendar(
            @RequestParam(defaultValue = "0") int year,
            @RequestParam(defaultValue = "0") int month) {
        if (year == 0) year = LocalDate.now().getYear();
        if (month == 0) month = LocalDate.now().getMonthValue();

        return ApiResponse.onSuccess("채용공고 캘린더 조회 성공",
                jobPostingService.getCalendar(year, month));
    }
}
