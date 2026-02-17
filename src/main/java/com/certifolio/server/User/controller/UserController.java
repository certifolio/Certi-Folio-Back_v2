package com.certifolio.server.User.controller;

import com.certifolio.server.User.domain.User;
import com.certifolio.server.User.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.springframework.web.multipart.MultipartFile;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/user")
public class UserController {

    private final UserRepository userRepository;

    // 이미지 저장 경로 (실제 운영에서는 S3 등 클라우드 스토리지 사용 권장)
    private static final String UPLOAD_DIR = "uploads/profile-images/";

    @GetMapping("/me")
    public ResponseEntity<?> getMyProfile(@AuthenticationPrincipal Object principal) {
        User user = getUser(principal);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "User not found"));
        }

        Map<String, Object> data = new HashMap<>();
        data.put("id", user.getId().toString());
        data.put("name", user.getName());
        data.put("email", user.getEmail());
        data.put("profileImage", user.getPicture());
        data.put("provider", user.getProvider());
        data.put("nickname", user.getNickname()); // Can be null, triggering popup on frontend

        // Defaults
        // Profile fields
        data.put("phone", user.getPhone());
        data.put("location", user.getLocation());
        data.put("university", user.getUniversity());
        data.put("major", user.getMajor());
        data.put("year", user.getYear());
        data.put("company", user.getCompany());
        data.put("bio", user.getBio());
        data.put("isInfoInputted", user.isInfoInputted());
        data.put("isAdmin", user.isAdmin());
        data.put("interests", new String[] {}); // Can be extended later

        return ResponseEntity.ok(Map.of("success", true, "data", data));
    }

    @PutMapping("/nickname")
    public ResponseEntity<?> setNickname(@AuthenticationPrincipal Object principal,
            @RequestBody Map<String, String> body) {
        User user = getUser(principal);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("success", false));
        }

        String nickname = body.get("nickname");
        if (nickname == null || nickname.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Nickname required"));
        }

        if (userRepository.existsByNickname(nickname)) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Nickname already exists"));
        }

        user.setNickname(nickname);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping("/nickname/check")
    public ResponseEntity<?> checkNickname(@RequestParam String nickname) {
        boolean exists = userRepository.existsByNickname(nickname);
        if (exists) {
            return ResponseEntity.ok(Map.of("success", true, "available", false, "message", "이미 사용 중인 닉네임입니다."));
        }
        return ResponseEntity.ok(Map.of("success", true, "available", true));
    }

    /**
     * 프로필 수정 API
     */
    @PatchMapping("/profile")
    public ResponseEntity<?> updateProfile(@AuthenticationPrincipal Object principal,
            @RequestBody Map<String, Object> body) {
        User user = getUser(principal);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "User not found"));
        }

        String nickname = (String) body.get("nickname");

        // Check nickname uniqueness if changed
        if (nickname != null && !nickname.equals(user.getNickname()) && userRepository.existsByNickname(nickname)) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "Nickname already exists"));
        }

        // Update profile
        user.updateProfile(
                nickname,
                (String) body.get("phone"),
                (String) body.get("location"),
                (String) body.get("university"),
                (String) body.get("major"),
                (String) body.get("year"),
                (String) body.get("company"),
                (String) body.get("bio"));

        User savedUser = userRepository.save(user);

        // Build response
        Map<String, Object> userData = new HashMap<>();
        userData.put("id", savedUser.getId().toString());
        userData.put("name", savedUser.getName());
        userData.put("email", savedUser.getEmail());
        userData.put("nickname", savedUser.getNickname());
        userData.put("profileImage", savedUser.getPicture());
        userData.put("phone", savedUser.getPhone());
        userData.put("location", savedUser.getLocation());
        userData.put("university", savedUser.getUniversity());
        userData.put("major", savedUser.getMajor());
        userData.put("year", savedUser.getYear());
        userData.put("company", savedUser.getCompany());
        userData.put("bio", savedUser.getBio());

        return ResponseEntity.ok(Map.of("success", true, "user", userData));
    }

    /**
     * 프로필 이미지 업로드 API
     */
    @PostMapping("/profile-image")
    public ResponseEntity<?> uploadProfileImage(
            @AuthenticationPrincipal Object principal,
            @RequestParam("image") MultipartFile file) {

        User user = getUser(principal);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "User not found"));
        }

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("success", false, "message", "File is empty"));
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "Only image files are allowed"));
        }

        // Validate file size (5MB max)
        if (file.getSize() > 5 * 1024 * 1024) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "message", "File size must be less than 5MB"));
        }

        try {
            // Create upload directory if not exists
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename
            String originalFilename = file.getOriginalFilename();
            String extension = originalFilename != null && originalFilename.contains(".")
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".jpg";
            String newFilename = user.getId() + "_" + UUID.randomUUID() + extension;

            // Save file
            Path filePath = uploadPath.resolve(newFilename);
            Files.copy(file.getInputStream(), filePath);

            // Update user profile image URL
            String imageUrl = "/uploads/profile-images/" + newFilename;
            user.setPicture(imageUrl);
            userRepository.save(user);

            return ResponseEntity.ok(Map.of(
                    "success", true,
                    "profileImageUrl", imageUrl));

        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body(Map.of("success", false, "message", "Failed to upload image"));
        }
    }

    /**
     * 프로필 이미지 삭제 API
     */
    @DeleteMapping("/profile-image")
    public ResponseEntity<?> deleteProfileImage(@AuthenticationPrincipal Object principal) {
        User user = getUser(principal);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "User not found"));
        }

        user.setPicture(null);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("success", true, "message", "Profile image deleted"));
    }

    /**
     * 계정 삭제 API
     */
    @DeleteMapping("/account")
    public ResponseEntity<?> deleteAccount(@AuthenticationPrincipal Object principal) {
        User user = getUser(principal);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "User not found"));
        }

        return ResponseEntity.ok(Map.of("success", true, "message", "Account deleted successfully"));
    }

    /**
     * 기본 정보 업데이트 (이름, 정보 입력 완료 여부)
     */
    @PatchMapping("/basic-info")
    public ResponseEntity<?> updateBasicInfo(@AuthenticationPrincipal Object principal,
                                             @RequestBody Map<String, Object> body) {
        User user = getUser(principal);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "User not found"));
        }

        String name = (String) body.get("name");
        Boolean isInfoInputted = (Boolean) body.get("isInfoInputted");

        user.updateBasicInfo(name, isInfoInputted != null ? isInfoInputted : true);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("success", true, "message", "Basic info updated"));
    }

    private User getUser(Object principal) {
        String subject = null;
        if (principal instanceof UserDetails) {
            subject = ((UserDetails) principal).getUsername();
        } else if (principal instanceof String) {
            subject = (String) principal;
        }

        if (subject == null)
            return null;

        // Token subject is always "provider:providerId" format
        if (subject.contains(":")) {
            String[] parts = subject.split(":", 2);
            return userRepository.findByProviderAndProviderId(parts[0], parts[1]).orElse(null);
        } else {
            // Legacy fallback: try email
            return userRepository.findByEmail(subject).orElse(null);
        }
    }
}
