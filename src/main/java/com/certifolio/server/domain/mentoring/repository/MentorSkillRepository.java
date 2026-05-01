package com.certifolio.server.domain.mentoring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.certifolio.server.domain.mentoring.entity.MentorSkill;

@Repository
public interface MentorSkillRepository extends JpaRepository<MentorSkill, Long> {
    // Mentor 엔티티의 CascadeType.ALL + orphanRemoval=true 를 통해 관리되므로
    // 별도의 조회/삭제 메서드가 불필요합니다.
}
