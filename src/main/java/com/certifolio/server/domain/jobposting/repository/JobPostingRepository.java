package com.certifolio.server.domain.jobposting.repository;

import com.certifolio.server.domain.jobposting.entity.JobPosting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface JobPostingRepository extends JpaRepository<JobPosting, Long> {

    Optional<JobPosting> findByLink(String link);

    List<JobPosting> findByStartDateLessThanEqualAndEndDateGreaterThanEqual(
            LocalDate endOfMonth, LocalDate startOfMonth);
}
