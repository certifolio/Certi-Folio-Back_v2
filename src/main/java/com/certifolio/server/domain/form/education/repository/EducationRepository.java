package com.certifolio.server.domain.form.education.repository;

import com.certifolio.server.domain.form.education.entity.Education;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EducationRepository extends JpaRepository<Education, Long> {
    List<Education> findAllByUserId(Long userId);
    Optional<Education> findTopByUserIdOrderByCreatedAtDesc(Long userId);
}
