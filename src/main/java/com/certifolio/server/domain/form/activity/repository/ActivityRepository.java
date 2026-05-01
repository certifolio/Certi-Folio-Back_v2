package com.certifolio.server.domain.form.activity.repository;

import com.certifolio.server.domain.form.activity.entity.Activity;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;


public interface ActivityRepository extends JpaRepository<Activity, Long> {
    List<Activity> findAllByUserId(Long userId);
    List<Activity> findAllByUserIdOrderByEndMonthDesc(Long userId);
}
