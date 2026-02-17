package com.certifolio.server.Form.Education.repository;

import com.certifolio.server.Form.Education.domain.Education;
import com.certifolio.server.User.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface EducationRepository extends JpaRepository<Education, Long> {
    Optional<Education> findByUser(User user);

    Optional<Education> findByUserId(Long userId);
}
