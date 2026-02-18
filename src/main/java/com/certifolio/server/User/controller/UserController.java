package com.certifolio.server.User.controller;

import com.certifolio.server.User.domain.User;
import com.certifolio.server.User.repository.UserRepository;
import com.certifolio.server.Form.util.AuthenticationHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    
    // Removing AuthenticationHelper usage for now as it delegates to custom logic
    // private final AuthenticationHelper authenticationHelper; 

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
        User user = getUser(principal);
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

    @PatchMapping("/basic-info")
    public ResponseEntity<?> updateBasicInfo(@AuthenticationPrincipal Object principal,
                                             @RequestBody Map<String, Object> body) {
        User user = getUser(principal);
        if (user == null) {
            return ResponseEntity.status(401).body(Map.of("success", false, "message", "User not found"));
        }
        
        String name = (String) body.get("name");
        Integer birthYear = null;
        
        if (body.get("birthYear") != null) {
            try {
                birthYear = Integer.parseInt(body.get("birthYear").toString());
            } catch (NumberFormatException e) {
                // Ignore or throw bad request
            }
        }

        user.updateBasicInfo(name, birthYear, true);
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("success", true, "message", "Information updated successfully"));
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

        if (subject.contains(":")) {
            String[] parts = subject.split(":", 2);
            return userRepository.findByProviderAndProviderId(parts[0], parts[1]).orElse(null);
        } else {
            return userRepository.findByEmail(subject).orElse(null);
        }
    }
}
