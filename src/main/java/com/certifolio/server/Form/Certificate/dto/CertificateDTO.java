package com.certifolio.server.Form.Certificate.dto;

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
    private String issuer;
    private String issueDate;
    private String expiryDate;
    private String status;
    private String score;
    private String certificateNumber;
    private String category;
    private String imageUrl;
}
