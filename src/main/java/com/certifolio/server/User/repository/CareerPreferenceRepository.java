package com.certifolio.server.User.repository;

import com.certifolio.server.User.domain.CareerPreference;
import com.certifolio.server.User.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CareerPreferenceRepository extends JpaRepository<CareerPreference, Long> {
    Optional<CareerPreference> findByUser(User user);
}
