package com.certifolio.server.domain.form.project.repository;

import com.certifolio.server.domain.form.project.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findAllByUserId(Long userId);
    List<Project> findAllByUserIdOrderByEndDateDesc(Long userId);
}
