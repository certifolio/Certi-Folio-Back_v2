package com.certifolio.server.Form.Activity.controller;

import com.certifolio.server.Form.Activity.dto.ActivityDTO;
import com.certifolio.server.Form.Activity.service.ActivityService;
import com.certifolio.server.Form.util.AuthenticationHelper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/portfolio/activities")
@RequiredArgsConstructor
public class ActivityController {

    private final ActivityService activityService;
    private final AuthenticationHelper authenticationHelper;

    @PostMapping
    public ResponseEntity<?> saveActivities(@AuthenticationPrincipal Object principal,
            @RequestBody List<ActivityDTO> dtos) {
        Long userId = authenticationHelper.getUserId(principal);
        activityService.saveActivities(userId, dtos);
        return ResponseEntity.ok(Map.of("success", true));
    }

    @GetMapping
    public ResponseEntity<?> getActivities(@AuthenticationPrincipal Object principal) {
        Long userId = authenticationHelper.getUserId(principal);
        return ResponseEntity.ok(Map.of("success", true, "data", activityService.getActivities(userId)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateActivity(@AuthenticationPrincipal Object principal, @PathVariable Long id, @RequestBody ActivityDTO dto) {
        Long userId = authenticationHelper.getUserId(principal);
        ActivityDTO updated = activityService.updateActivity(userId, id, dto);
        return ResponseEntity.ok(Map.of("success", true, "activity", updated));
    }

    @PostMapping("/add")
    public ResponseEntity<?> addActivity(@AuthenticationPrincipal Object principal, @RequestBody ActivityDTO dto) {
        Long userId = authenticationHelper.getUserId(principal);
        ActivityDTO saved = activityService.addActivity(userId, dto);
        return ResponseEntity.ok(Map.of("success", true, "activity", saved));   
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteActivity(@AuthenticationPrincipal Object principal, @PathVariable Long id) {
        Long userId = authenticationHelper.getUserId(principal);
        activityService.deleteActivity(userId, id);
        return ResponseEntity.ok(Map.of("success", true));
    }
}
