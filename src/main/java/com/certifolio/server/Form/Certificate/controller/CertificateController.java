package com.certifolio.server.Form.Certificate.controller;

import com.certifolio.server.Form.Certificate.dto.CertificateDTO;
import com.certifolio.server.Form.Certificate.service.CertificateService;
import com.certifolio.server.Form.util.AuthenticationHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/portfolio/certificates")
@RequiredArgsConstructor
public class CertificateController {

    private final CertificateService certificateService;
    private final AuthenticationHelper authenticationHelper;

    @PostMapping
    public ResponseEntity<?> saveCertificates(@AuthenticationPrincipal Object principal,
                                              @RequestBody List<CertificateDTO> dtos) {
        Long userId = authenticationHelper.getUserId(principal);
        certificateService.saveCertificates(userId, dtos);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping
    public ResponseEntity<?> getCertificates(@AuthenticationPrincipal Object principal) {
        Long userId = authenticationHelper.getUserId(principal);
        return ResponseEntity.ok(Map.of("success", true, "data", certificateService.getCertificates(userId)));
    }

    @PostMapping("/add")
    public ResponseEntity<?> addCertificate(@AuthenticationPrincipal Object principal,
                                            @RequestBody CertificateDTO dto) {
        Long userId = authenticationHelper.getUserId(principal);
        CertificateDTO saved = certificateService.addCertificate(userId, dto);
        return ResponseEntity.ok(Map.of("success", true, "certificate", saved));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCertificate(@AuthenticationPrincipal Object principal, @PathVariable Long id) {
        Long userId = authenticationHelper.getUserId(principal);
        certificateService.deleteCertificate(userId, id);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCertificate(@AuthenticationPrincipal Object principal,
                                            @PathVariable Long id,
                                            @RequestBody CertificateDTO dto) {
        Long userId = authenticationHelper.getUserId(principal);
        CertificateDTO updated = certificateService.updateCertificate(userId, id, dto);
        return ResponseEntity.ok(Map.of("success", true, "certificate", updated));
    }
    
}
