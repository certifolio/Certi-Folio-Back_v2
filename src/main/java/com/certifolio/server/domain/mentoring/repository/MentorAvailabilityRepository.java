package com.certifolio.server.Mentoring.repository;

import com.certifolio.server.Mentoring.domain.MentorAvailability;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MentorAvailabilityRepository extends JpaRepository<MentorAvailability, Long> {
    // Mentor 엔티티의 CascadeType.ALL + orphanRemoval=true 를 통해 관리되므로
    // 별도의 조회/삭제 메서드가 불필요합니다.
}
