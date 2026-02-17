package com.certifolio.server.Form.Certificate.repository;

import com.certifolio.server.Form.Certificate.domain.Certificate;
import com.certifolio.server.User.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;

import java.util.List;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    List<Certificate> findAllByUser(User user);

    List<Certificate> findAllByUserId(Long userId);

    List<Certificate> findByExpiryDate(LocalDate expiryDate);

    List<Certificate> findByExpiryDateIsNullAndIssueDate(LocalDate issueDate);
}
