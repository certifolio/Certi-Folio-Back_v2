package com.certifolio.server.domain.user.repository;

import com.certifolio.server.domain.user.entity.CareerPreference;
import com.certifolio.server.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CareerPreferenceRepository extends JpaRepository<CareerPreference, Long> {
    Optional<CareerPreference> findByUser(User user);
}

