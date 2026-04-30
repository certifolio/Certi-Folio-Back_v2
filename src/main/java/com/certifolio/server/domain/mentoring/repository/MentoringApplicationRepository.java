package com.certifolio.server.domain.mentoring.repository;

import com.certifolio.server.domain.mentoring.entity.ApplicationStatus;
import com.certifolio.server.domain.mentoring.entity.MentoringApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MentoringApplicationRepository extends JpaRepository<MentoringApplication, Long> {

        // 멘토가 받은 신청 목록
        @Query("SELECT a FROM MentoringApplication a WHERE a.mentor.id = :mentorId ORDER BY a.createdAt DESC")
        List<MentoringApplication> findByMentorId(@Param("mentorId") Long mentorId);

        // 멘티가 보낸 신청 목록
        @Query("SELECT a FROM MentoringApplication a WHERE a.mentee.id = :menteeId ORDER BY a.createdAt DESC")
        List<MentoringApplication> findByMenteeId(@Param("menteeId") Long menteeId);


        // 중복 신청 체크
        @Query("SELECT COUNT(a) > 0 FROM MentoringApplication a WHERE a.mentee.id = :menteeId AND a.mentor.id = :mentorId AND a.status = :status")
        boolean existsByMenteeIdAndMentorIdAndStatus(
                        @Param("menteeId") Long menteeId,
                        @Param("mentorId") Long mentorId,
                        @Param("status") ApplicationStatus status);

        // 승인된 멘토링 관계 확인 (멘티로서 승인됨)
        @Query("SELECT COUNT(a) > 0 FROM MentoringApplication a WHERE a.mentee.id = :userId AND a.mentor.id = :mentorId AND a.status = 'APPROVED'")
        boolean existsApprovedApplication(
                        @Param("userId") Long userId,
                        @Param("mentorId") Long mentorId);
}
