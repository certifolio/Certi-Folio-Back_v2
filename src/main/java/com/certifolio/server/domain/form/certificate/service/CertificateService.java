package com.certifolio.server.domain.form.certificate.service;

import com.certifolio.server.domain.form.certificate.dto.request.CertificateRequestDTO;
import com.certifolio.server.domain.form.certificate.dto.response.CertificateResponseDTO;
import com.certifolio.server.domain.form.certificate.entity.Certificate;
import com.certifolio.server.domain.form.certificate.repository.CertificateRepository;
import com.certifolio.server.domain.user.entity.User;
import com.certifolio.server.domain.user.service.UserService;
import com.certifolio.server.global.apiPayload.code.GeneralErrorCode;
import com.certifolio.server.global.apiPayload.exception.BusinessException;
import com.certifolio.server.global.common.util.DateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CertificateService {

    private final UserService userService;
    private final CertificateRepository certificateRepository;

    // 자격증 전체 조회
    @Transactional(readOnly = true)
    public List<CertificateResponseDTO> getCertificates(Long userId) {
        return certificateRepository.findAllByUserId(userId).stream()
                .map(CertificateResponseDTO::from)
                .toList();
    }

    // 자격증 전체 저장 (최초)
    @Transactional
    public void saveCertificate(Long userId, List<CertificateRequestDTO> request) {
        User user = userService.getUserById(userId);

        if (request == null || request.isEmpty()) {
            throw new BusinessException(GeneralErrorCode.CERTIFICATE_NOT_INPUTTED);
        }

        List<Certificate> certificates = request.stream()
                .map(dto -> Certificate.builder()
                        .user(user)
                        .name(dto.name())
                        .type(dto.type())
                        .issuer(dto.issuer())
                        .issueDate(DateUtils.parseDate(dto.issueDate()))
                        .score(dto.score())
                        .certificateNumber(dto.certificateNumber())
                        .build())
                .toList();

        certificateRepository.saveAll(certificates);
    }

    // 자격증 단건 조회
    @Transactional(readOnly = true)
    public CertificateResponseDTO getCertificate(Long userId, Long certificateId) {
        Certificate certificate = getCertificateWithOwnerCheck(userId, certificateId);
        return CertificateResponseDTO.from(certificate);
    }

    // 자격증 단건 추가
    @Transactional
    public CertificateResponseDTO addCertificate(Long userId, CertificateRequestDTO request) {
        User user = userService.getUserById(userId);
        Certificate certificate = Certificate.builder()
                .user(user)
                .name(request.name())
                .type(request.type())
                .issuer(request.issuer())
                .issueDate(DateUtils.parseDate(request.issueDate()))
                .score(request.score())
                .certificateNumber(request.certificateNumber())
                .build();
        return CertificateResponseDTO.from(certificateRepository.save(certificate));
    }

    // 자격증 단건 수정
    @Transactional
    public CertificateResponseDTO modifyCertificate(Long userId, Long certificateId, CertificateRequestDTO request) {
        Certificate certificate = getCertificateWithOwnerCheck(userId, certificateId);
        certificate.update(
                request.name(),
                request.type(),
                request.issuer(),
                DateUtils.parseDate(request.issueDate()),
                request.score(),
                request.certificateNumber()
        );
        return CertificateResponseDTO.from(certificate);
    }

    // 자격증 단건 삭제
    @Transactional
    public void deleteCertificate(Long userId, Long certificateId) {
        Certificate certificate = getCertificateWithOwnerCheck(userId, certificateId);
        certificateRepository.delete(certificate);
    }

    // 자격증 확인
    private Certificate getCertificateWithOwnerCheck(Long userId, Long certificateId) {
        Certificate certificate = certificateRepository.findById(certificateId)
                .orElseThrow(() -> new BusinessException(GeneralErrorCode.CERTIFICATE_NOT_FOUND));
        if (!certificate.getUser().getId().equals(userId)) {
            throw new BusinessException(GeneralErrorCode.CERTIFICATE_UNAUTHORIZED);
        }
        return certificate;
    }
}
