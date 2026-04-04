package com.certifolio.server.Mentoring.repository;

import com.certifolio.server.Mentoring.domain.MentoringSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MentoringSessionRepository extends JpaRepository<MentoringSession, Long> {

        @Query("SELECT s FROM MentoringSession s WHERE s.mentee.id = :userId OR s.mentor.user.id = :userId ORDER BY s.createdAt DESC")
        List<MentoringSession> findByUserIdOrderByCreatedAtDesc(@Param("userId") Long userId);
}
