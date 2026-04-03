package com.certifolio.server.domain.form.certificate.dto.response;

import com.certifolio.server.domain.form.certificate.entity.Certificate;
import com.certifolio.server.global.common.util.DateUtils;

public record CertificateResponseDTO(
        Long id,
        String name,
        String type,
        String issuer,
        String issueDate,
        String expiryDate,
        String score,
        String certificateNumber
) {
    public static CertificateResponseDTO from(Certificate certificate) {
        return new CertificateResponseDTO(
                certificate.getId(),
                certificate.getName(),
                certificate.getType(),
                certificate.getIssuer(),
                DateUtils.dateToString(certificate.getIssueDate()),
                certificate.getExpiryDate() != null ? DateUtils.dateToString(certificate.getExpiryDate()) : null,
                certificate.getScore(),
                certificate.getCertificateNumber()
        );
    }
}
