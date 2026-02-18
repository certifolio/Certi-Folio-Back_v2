package com.certifolio.server.Form.Activity.repository;

import com.certifolio.server.Form.Activity.domain.Activity;
import com.certifolio.server.User.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findAllByUser(User user);
}
