package com.certifolio.server.Form.Education.repository;

import com.certifolio.server.Form.Education.domain.Education;
import com.certifolio.server.User.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EducationRepository extends JpaRepository<Education, Long> {
    List<Education> findAllByUser(User user);

    List<Education> findByUserId(Long userId);
}
