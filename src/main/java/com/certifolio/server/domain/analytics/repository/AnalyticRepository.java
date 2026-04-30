package com.certifolio.server.domain.analytics.repository;

import com.certifolio.server.domain.analytics.entity.Analytic;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnalyticRepository extends JpaRepository<Analytic, Long> {

    Optional<Analytic> findTopByUserIdOrderByCreatedAtDesc(Long userId);

    List<Analytic> findAllByUserIdOrderByCreatedAtDesc(Long userId);
}
