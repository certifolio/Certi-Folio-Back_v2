package com.certifolio.server.Form.Certificate.service;

import com.certifolio.server.User.domain.User;
import com.certifolio.server.User.repository.UserRepository;
import com.certifolio.server.Form.Certificate.domain.Certificate;
import com.certifolio.server.Form.Certificate.dto.CertificateDTO;
import com.certifolio.server.Form.util.DateUtils;
import com.certifolio.server.Form.Certificate.repository.CertificateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CertificateService {

    private final UserRepository userRepository;
    private final CertificateRepository certificateRepository;

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    /**
     * 자격증 전체 저장 (기존 삭제 후 재저장)
     */
    public void saveCertificates(Long userId, List<CertificateDTO> dtos) {
        User user = getUser(userId);

        // 기존 데이터 삭제
        List<Certificate> oldList = certificateRepository.findAllByUser(user);
        certificateRepository.deleteAll(oldList);

        if (dtos == null || dtos.isEmpty()) {
            return;
        }

        List<Certificate> certificates = dtos.stream()
                .map(dto -> {
                    LocalDate issueDate = DateUtils.parseDate(dto.getIssueDate());

                    return Certificate.builder()
                            .user(user)
                            .name(dto.getName())
                            .type(dto.getType())
                            .issuer(dto.getIssuer())
                            .issueDate(issueDate)
                            .score(dto.getScore())
                            .certificateNumber(dto.getCertificateNumber())
                            .build();
                })
                .collect(Collectors.toList());

        certificateRepository.saveAll(certificates);
    }

    /**
     * 자격증 목록 조회
     */
    public List<CertificateDTO> getCertificates(Long userId) {
        return certificateRepository.findAllByUserId(userId).stream()
                .map(CertificateDTO::from)
                .collect(Collectors.toList());
    }

    /**
     * 자격증 단건 추가
     */
    public CertificateDTO addCertificate(Long userId, CertificateDTO dto) {
        User user = getUser(userId);

        LocalDate issueDate = DateUtils.parseDate(dto.getIssueDate());

        Certificate cert = Certificate.builder()
                .user(user)
                .name(dto.getName())
                .issuer(dto.getIssuer())
                .issueDate(issueDate)
                .score(dto.getScore())
                .certificateNumber(dto.getCertificateNumber())
                .type(dto.getType())
                .build();

        Certificate saved = certificateRepository.save(cert);

        return CertificateDTO.from(saved);
    }

    /**
     * 자격증 삭제
     */
    public void deleteCertificate(Long userId, Long certificateId) {
        Certificate cert = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new RuntimeException("Certificate not found"));

        if (!cert.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        certificateRepository.delete(cert);
    }

    /**
     * 자격증 업데이트
     */
    public CertificateDTO updateCertificate(Long userId, Long id, CertificateDTO dto) {
        Certificate cert = certificateRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Certificate not found"));

        if (!cert.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        cert.update(dto.getName(), dto.getType(), dto.getIssuer(), DateUtils.parseDate(dto.getIssueDate()), dto.getScore(), dto.getCertificateNumber());

        Certificate updated = certificateRepository.save(cert);

        return CertificateDTO.from(updated);
    }


}