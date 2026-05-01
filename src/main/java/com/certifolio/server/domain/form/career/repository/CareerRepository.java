package com.certifolio.server.domain.form.career.repository;

import com.certifolio.server.domain.form.career.entity.Career;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CareerRepository extends JpaRepository<Career, Long> {
    List<Career> findAllByUserId(Long userId);
    List<Career> findAllByUserIdOrderByEndDateDesc(Long userId);
}
