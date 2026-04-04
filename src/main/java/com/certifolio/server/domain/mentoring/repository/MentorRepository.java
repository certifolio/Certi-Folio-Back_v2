package com.certifolio.server.domain.mentoring.repository;

import com.certifolio.server.domain.mentoring.entity.Mentor;
import com.certifolio.server.domain.mentoring.entity.MentorStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MentorRepository extends JpaRepository<Mentor, Long> {

    Optional<Mentor> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    List<Mentor> findByStatus(MentorStatus status);

    @Query("SELECT m FROM Mentor m JOIN m.skills s WHERE s.skillName IN :skills AND m.status = 'APPROVED'")
    List<Mentor> findBySkillsContaining(@Param("skills") List<String> skills);
}
