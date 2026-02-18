package com.certifolio.server.Mentoring.repository;

import com.certifolio.server.Mentoring.domain.Mentor;
import com.certifolio.server.Mentoring.domain.MentorStatus;
import com.certifolio.server.User.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MentorRepository extends JpaRepository<Mentor, Long> {

    Optional<Mentor> findByUser(User user);

    Optional<Mentor> findByUserId(Long userId);

    boolean existsByUserId(Long userId);

    List<Mentor> findByStatus(MentorStatus status);

    @Query("SELECT m FROM Mentor m JOIN m.skills s WHERE s.skillName IN :skills AND m.status = 'APPROVED'")
    List<Mentor> findBySkillsContaining(@Param("skills") List<String> skills);

    @Query("SELECT m FROM Mentor m WHERE m.status = 'APPROVED' ORDER BY m.createdAt DESC")
    List<Mentor> findTopMentors();
}
