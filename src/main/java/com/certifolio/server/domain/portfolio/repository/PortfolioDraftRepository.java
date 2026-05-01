package com.certifolio.server.domain.portfolio.repository;

import com.certifolio.server.domain.portfolio.entity.PortfolioDraft;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PortfolioDraftRepository extends JpaRepository<PortfolioDraft, Long> {
    Optional<PortfolioDraft> findTopByUserIdOrderByCreatedAtDesc(Long userId);
}
