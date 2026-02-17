package com.certifolio.server.Form.Career.repository;

import com.certifolio.server.Form.Career.domain.Career;
import com.certifolio.server.User.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CareerRepository extends JpaRepository<Career, Long> {
    List<Career> findAllByUser(User user);

    List<Career> findByUserId(Long userId);
}
