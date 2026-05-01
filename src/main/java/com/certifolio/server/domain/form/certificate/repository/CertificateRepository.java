package com.certifolio.server.domain.form.certificate.repository;

import com.certifolio.server.domain.form.certificate.entity.Certificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface CertificateRepository extends JpaRepository<Certificate, Long> {
    List<Certificate> findAllByUserId(Long userId);
    List<Certificate> findByExpiryDate(LocalDate expiryDate);
    List<Certificate> findAllByUserIdOrderByIssueDateDesc(Long userId);
}
