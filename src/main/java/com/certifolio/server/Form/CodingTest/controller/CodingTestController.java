package com.certifolio.server.Form.CodingTest.controller;

import com.certifolio.server.Form.CodingTest.dto.CodingTestDTO;
import com.certifolio.server.Form.CodingTest.dto.SolvedAcResponseDTO;
import com.certifolio.server.Form.CodingTest.service.CodingTestService;
import com.certifolio.server.Form.util.AuthenticationHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/coding-test")
@RequiredArgsConstructor
public class CodingTestController {

    private final CodingTestService codingTestService;
    private final AuthenticationHelper authenticationHelper;

    /**
     * Check handle and get preview data
     */
    @GetMapping("/check")
    public ResponseEntity<?> checkHandle(@RequestParam String handle) {
        SolvedAcResponseDTO data = codingTestService.fetchSolvedAcData(handle);
        return ResponseEntity.ok(Map.of("success", true, "data", data));
    }

    /**
     * Get saved coding test data
     */
    @GetMapping
    public ResponseEntity<?> getCodingTest(@AuthenticationPrincipal Object principal) {
        Long userId = authenticationHelper.getUserId(principal);
        CodingTestDTO data = codingTestService.getCodingTest(userId);
        return ResponseEntity.ok(Map.of("success", true, "data", data != null ? data : "null"));
    }

    /**
     * Save verified handle
     */
    @PostMapping
    public ResponseEntity<?> saveCodingTest(@AuthenticationPrincipal Object principal,
                                            @RequestBody Map<String, String> request) {
        Long userId = authenticationHelper.getUserId(principal);
        String handle = request.get("handle");

        if (handle == null || handle.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Handle is required"));
        }

        codingTestService.saveCodingTest(userId, handle);
        return ResponseEntity.ok(Map.of("success", true, "message", "Saved successfully"));
    }
}
