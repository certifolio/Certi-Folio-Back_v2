package com.certifolio.server.Form.Activity.service;

import com.certifolio.server.User.domain.User;
import com.certifolio.server.User.repository.UserRepository;
import com.certifolio.server.Form.Activity.domain.Activity;
import com.certifolio.server.Form.Activity.dto.ActivityDTO;
import com.certifolio.server.Form.util.DateUtils;
import com.certifolio.server.Form.Activity.repository.ActivityRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ActivityService {

    private final UserRepository userRepository;
    private final ActivityRepository activityRepository;

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    /**
     * 활동 전체 저장 (기존 삭제 후 재저장)
     */
    public void saveActivities(Long userId, List<ActivityDTO> dtos) {
        User user = getUser(userId);

        // 기존 데이터 삭제
        activityRepository.findAllByUser(user).forEach(activityRepository::delete);

        if (dtos == null || dtos.isEmpty()) {
            return;
        }

        List<Activity> activities = dtos.stream()
                .map(dto -> Activity.builder()
                        .user(user)
                        .name(dto.getName())
                        .type(dto.getType())
                        .role(dto.getRole())
                        .startDate(DateUtils.parseDate(dto.getStartDate()))
                        .endDate(DateUtils.parseDate(dto.getEndDate()))
                        .description(dto.getDescription())
                        .result(dto.getResult())
                        .build())
                .collect(Collectors.toList());

        activityRepository.saveAll(activities);
    }

    /**
     * 활동 목록 조회
     */
    public List<ActivityDTO> getActivities(Long userId) {
        User user = getUser(userId);
        return activityRepository.findAllByUser(user).stream()
                .map(ActivityDTO::from)
                .collect(Collectors.toList());
    }

    public ActivityDTO addActivity(Long userId, ActivityDTO dto) {
        User user = getUser(userId);

        LocalDate startDate = DateUtils.parseDate(dto.getStartDate());
        LocalDate endDate = DateUtils.parseDate(dto.getEndDate());

        Activity activity = Activity.builder()
                .user(user)
                .name(dto.getName())
                .type(dto.getType())
                .role(dto.getRole())
                .startDate(startDate)
                .endDate(endDate)
                .description(dto.getDescription())
                .result(dto.getResult())
                .build();

        Activity saved = activityRepository.save(activity);

        return ActivityDTO.from(saved);
    }

    public void deleteActivity(Long userId, Long activityId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new IllegalArgumentException("Activity not found"));

        if (!activity.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        activityRepository.delete(activity);
    }

    public ActivityDTO updateActivity(Long userId, Long id, ActivityDTO dto) {
        Activity activity = activityRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Activity not found"));

        if (!activity.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        LocalDate startDate = DateUtils.parseDate(dto.getStartDate());
        LocalDate endDate = DateUtils.parseDate(dto.getEndDate());

        activity.update(dto.getName(), dto.getType(), dto.getRole(), startDate, endDate, dto.getDescription(), dto.getResult());

        Activity updated = activityRepository.save(activity);

        return ActivityDTO.from(updated);

    }

    private void validateUser(Activity activity, Long userId) {
        if (!activity.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }
    }
}
