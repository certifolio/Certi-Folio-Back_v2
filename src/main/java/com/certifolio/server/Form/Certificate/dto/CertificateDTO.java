package com.certifolio.server.Form.Certificate.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CertificateDTO {
    private Long id;
    private String name;
    private String type;
    private String issuer;
    @JsonAlias("date")
    private String issueDate;
    private String expiryDate;
    private String score;
    @JsonAlias("certId")
    private String certificateNumber;

    public static CertificateDTO from(com.certifolio.server.Form.Certificate.domain.Certificate certificate) {
        return CertificateDTO.builder()
                .id(certificate.getId())
                .name(certificate.getName())
                .type(certificate.getType())
                .issuer(certificate.getIssuer())
                .issueDate(certificate.getIssueDate() != null ? certificate.getIssueDate().toString() : null)
                .expiryDate(certificate.getExpiryDate() != null ? certificate.getExpiryDate().toString() : null)
                .score(certificate.getScore())
                .certificateNumber(certificate.getCertificateNumber())
                .build();
    }
}
