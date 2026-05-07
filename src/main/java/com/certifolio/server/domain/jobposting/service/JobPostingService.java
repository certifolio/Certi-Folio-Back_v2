package com.certifolio.server.domain.jobposting.service;

import com.certifolio.server.domain.jobposting.dto.response.JobPostingCalendarResponseDTO;
import com.certifolio.server.domain.jobposting.dto.response.JobPostingResponseDTO;
import com.certifolio.server.domain.jobposting.entity.JobPosting;
import com.certifolio.server.domain.jobposting.repository.JobPostingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class JobPostingService {

    private final JobPostingRepository jobPostingRepository;

    @Transactional
    public int importJobPostings(Map<String, Map<String, String>> data) {
        List<JobPosting> toSave = new ArrayList<>();

        for (Map.Entry<String, Map<String, String>> entry : data.entrySet()) {
            String companyName = entry.getKey();
            Map<String, String> fields = entry.getValue();

            String link = fields.get("link");
            LocalDate[] dates = parsePlan(fields.get("plan"));

            jobPostingRepository.findByLink(link).ifPresentOrElse(
                    existing -> {
                        existing.update(companyName, fields.get("state"), fields.get("content"),
                                fields.get("position"), dates[0], dates[1]);
                        toSave.add(existing);
                    },
                    () -> toSave.add(JobPosting.builder()
                            .companyName(companyName)
                            .state(fields.get("state"))
                            .content(fields.get("content"))
                            .position(fields.get("position"))
                            .startDate(dates[0])
                            .endDate(dates[1])
                            .link(link)
                            .build())
            );
        }

        jobPostingRepository.saveAll(toSave);
        return toSave.size();
    }

    @Transactional(readOnly = true)
    public JobPostingCalendarResponseDTO getCalendar(int year, int month) {
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDate firstDay = yearMonth.atDay(1);
        LocalDate lastDay = yearMonth.atEndOfMonth();

        List<JobPostingResponseDTO> jobPostings = jobPostingRepository
                .findByStartDateLessThanEqualAndEndDateGreaterThanEqual(lastDay, firstDay)
                .stream()
                .map(JobPostingResponseDTO::from)
                .toList();

        return new JobPostingCalendarResponseDTO(year, month, jobPostings);
    }

    private LocalDate[] parsePlan(String plan) {
        String[] parts = plan.split("~");
        String[] startParts = parts[0].trim().split("/");
        String[] endParts = parts[1].trim().split("/");

        int year = LocalDate.now().getYear();
        int startMonth = Integer.parseInt(startParts[0]);
        int startDay = Integer.parseInt(startParts[1]);
        int endMonth = Integer.parseInt(endParts[0]);
        int endDay = Integer.parseInt(endParts[1]);

        LocalDate startDate = LocalDate.of(year, startMonth, startDay);
        LocalDate endDate = LocalDate.of(year, endMonth, endDay);

        if (endDate.isBefore(startDate)) {
            endDate = endDate.plusYears(1);
        }

        return new LocalDate[]{startDate, endDate};
    }
}
