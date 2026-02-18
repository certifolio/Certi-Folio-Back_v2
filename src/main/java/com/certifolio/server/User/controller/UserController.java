package com.certifolio.server.User.controller;

import com.certifolio.server.User.domain.User;
import com.certifolio.server.User.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<?> getUserProfile(@AuthenticationPrincipal Object principal) {
        return getProfileInternal(principal);
    }
    
    // Restore /me endpoint for backward compatibility
    @GetMapping("/me")
    public ResponseEntity<?> getUserProfileLegacy(@AuthenticationPrincipal Object principal) {
        return getProfileInternal(principal);
    }

    private ResponseEntity<?> getProfileInternal(Object principal) {
        User user = userService.getByPrincipal(principal);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "User not found"));
        }

        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId());
        data.put("name", user.getName());
        data.put("email", user.getEmail());
        data.put("picture", user.getPicture());
        data.put("role", user.getRole());
        data.put("provider", user.getProvider());
        data.put("birthYear", user.getBirthYear());
        data.put("isInfoInputted", user.isInfoInputted());

        return ResponseEntity.ok(Map.of("success", true, "data", data));
    }

    /**
     * 온보딩: 이름, 출생연도, 희망 직무, 희망 기업 유형을 한 번에 저장
     */
    @PatchMapping("/onboarding")
    public ResponseEntity<?> onboarding(@AuthenticationPrincipal Object principal,
                                        @RequestBody Map<String, Object> body) {
        User user = userService.getByPrincipal(principal);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "User not found"));
        }

        String name = (String) body.get("name");
        Integer birthYear = parseIntegerSafely(body.get("birthYear"));
        String jobRole = (String) body.get("jobRole");
        String companyType = (String) body.get("companyType");

        userService.saveOnboarding(user, name, birthYear, jobRole, companyType);

        return ResponseEntity.ok(Map.of("success", true, "message", "온보딩 정보가 저장되었습니다."));
    }

    @PatchMapping("/basic-info")
    public ResponseEntity<?> updateBasicInfo(@AuthenticationPrincipal Object principal,
                                             @RequestBody Map<String, Object> body) {
        User user = userService.getByPrincipal(principal);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "User not found"));
        }

        String name = (String) body.get("name");
        Integer birthYear = parseIntegerSafely(body.get("birthYear"));

        userService.updateBasicInfo(user, name, birthYear);

        return ResponseEntity.ok(Map.of("success", true, "message", "Information updated successfully"));
    }

    private Integer parseIntegerSafely(Object value) {
        if (value == null) return null;
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
