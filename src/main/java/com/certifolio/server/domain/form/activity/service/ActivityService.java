package com.certifolio.server.domain.form.activity.service;

import com.certifolio.server.domain.form.activity.dto.response.ActivityResponseDTO;
import com.certifolio.server.domain.form.activity.dto.request.ActivityRequestDTO;
import com.certifolio.server.domain.form.activity.entity.Activity;
import com.certifolio.server.domain.form.activity.repository.ActivityRepository;
import com.certifolio.server.domain.user.entity.User;
import com.certifolio.server.domain.user.service.UserService;
import com.certifolio.server.global.apiPayload.code.GeneralErrorCode;
import com.certifolio.server.global.apiPayload.exception.BusinessException;
import com.certifolio.server.global.common.util.DateUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ActivityService {

    private final UserService userService;
    private final ActivityRepository activityRepository;

    // 활동 전체 조회
    @Transactional(readOnly = true)
    public List<ActivityResponseDTO> getActivities(Long userId) {
        return activityRepository.findAllByUserId(userId).stream()
                .map(ActivityResponseDTO::from)
                .toList();
    }

    // 활동 전체 저장 (최초)
    @Transactional
    public void saveActivity(Long userId, List<ActivityRequestDTO> request) {
        User user = userService.getUserById(userId);

        if (request == null || request.isEmpty()) {
            throw new BusinessException(GeneralErrorCode.ACTIVITY_NOT_INPUTTED);
        }

        List<Activity> activities = request.stream()
                .map(requestDto -> Activity.builder()
                        .user(user)
                        .name(requestDto.name())
                        .type(requestDto.type())
                        .role(requestDto.role())
                        .startMonth(DateUtils.parseDate(requestDto.startMonth()))
                        .endMonth(DateUtils.parseDate(requestDto.endMonth()))
                        .description(requestDto.description())
                        .result(requestDto.result())
                        .build())
                .toList();

        activityRepository.saveAll(activities);
    }

    // 활동 단건 조회
    @Transactional(readOnly = true)
    public ActivityResponseDTO getActivity(Long userId, Long activityId) {
        Activity activity = getActivityWithOwnerCheck(userId, activityId);
        return ActivityResponseDTO.from(activity);
    }

    // 활동 단건 추가
    @Transactional
    public ActivityResponseDTO addActivity(Long userId, ActivityRequestDTO request) {
        User user = userService.getUserById(userId);
        Activity activity = Activity.builder()
                .user(user)
                .name(request.name())
                .type(request.type())
                .role(request.role())
                .startMonth(DateUtils.parseDate(request.startMonth()))
                .endMonth(DateUtils.parseDate(request.endMonth()))
                .description(request.description())
                .result(request.result())
                .build();
        return ActivityResponseDTO.from(activityRepository.save(activity));
    }

    // 활동 단건 수정
    @Transactional
    public ActivityResponseDTO modifyActivity(Long userId, Long activityId, ActivityRequestDTO request) {
        Activity activity = getActivityWithOwnerCheck(userId, activityId);
        activity.update(
                request.name(),
                request.type(),
                request.role(),
                DateUtils.parseDate(request.startMonth()),
                DateUtils.parseDate(request.endMonth()),
                request.description(),
                request.result()
        );
        return ActivityResponseDTO.from(activity);
    }

    // 활동 단건 삭제
    @Transactional
    public void deleteActivity(Long userId, Long activityId) {
        Activity activity = getActivityWithOwnerCheck(userId, activityId);
        activityRepository.delete(activity);
    }

    // 활동 확인
    private Activity getActivityWithOwnerCheck(Long userId, Long activityId) {
        Activity activity = activityRepository.findById(activityId)
                .orElseThrow(() -> new BusinessException(GeneralErrorCode.ACTIVITY_NOT_FOUND));
        if (!activity.getUser().getId().equals(userId)) {
            throw new BusinessException(GeneralErrorCode.ACTIVITY_UNAUTHORIZED);
        }
        return activity;
    }

}
