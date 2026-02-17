package com.certifolio.server.Form.Career.controller;

import com.certifolio.server.Form.Career.dto.CareerDTO;
import com.certifolio.server.Form.Career.service.CareerService;
import com.certifolio.server.Form.util.AuthenticationHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/portfolio/careers")
@RequiredArgsConstructor
public class CareerController {

    private final CareerService careerService;
    private final AuthenticationHelper authenticationHelper;

    @PostMapping
    public ResponseEntity<?> saveCareers(@AuthenticationPrincipal Object principal, @RequestBody List<CareerDTO> dtos) {
        Long userId = authenticationHelper.getUserId(principal);
        careerService.saveCareers(userId, dtos);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping
    public ResponseEntity<?> getCareers(@AuthenticationPrincipal Object principal) {
        Long userId = authenticationHelper.getUserId(principal);
        List<CareerDTO> careers = careerService.getCareers(userId);
        return ResponseEntity.ok(Map.of("success", true, "data", careers));
    }

    @PostMapping("/add")
    public ResponseEntity<?> addCareer(@AuthenticationPrincipal Object principal, @RequestBody CareerDTO dto) {
        Long userId = authenticationHelper.getUserId(principal);
        CareerDTO saved = careerService.addCareer(userId, dto);
        return ResponseEntity.ok(Map.of("success", true, "career", saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateCareer(@AuthenticationPrincipal Object principal, @PathVariable Long id, @RequestBody CareerDTO dto) {
        Long userId = authenticationHelper.getUserId(principal);
        CareerDTO updated = careerService.updateCareer(userId, id, dto);
        return ResponseEntity.ok(Map.of("success", true, "career", updated));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCareer(@AuthenticationPrincipal Object principal, @PathVariable Long id){
        Long userId = authenticationHelper.getUserId(principal);
        careerService.deleteCareer(userId, id);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
