package com.certifolio.server.Form.Project.repository;

import com.certifolio.server.Form.Project.domain.Project;
import com.certifolio.server.User.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProjectRepository extends JpaRepository<Project, Long> {
    List<Project> findAllByUser(User user);
}
