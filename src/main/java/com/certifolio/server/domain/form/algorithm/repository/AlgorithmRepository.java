package com.certifolio.server.domain.form.algorithm.repository;

import com.certifolio.server.domain.form.algorithm.entity.Algorithm;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AlgorithmRepository extends JpaRepository<Algorithm, Long> {
    Optional<Algorithm> findByUserId(Long userId);
}
