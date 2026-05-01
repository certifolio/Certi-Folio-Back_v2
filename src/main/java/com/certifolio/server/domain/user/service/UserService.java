package com.certifolio.server.domain.user.service;

import com.certifolio.server.domain.user.entity.CareerPreference;
import com.certifolio.server.domain.user.entity.User;
import com.certifolio.server.domain.user.repository.CareerPreferenceRepository;
import com.certifolio.server.domain.user.repository.UserRepository;
import com.certifolio.server.global.apiPayload.code.GeneralErrorCode;
import com.certifolio.server.global.apiPayload.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final CareerPreferenceRepository careerPreferenceRepository;

    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(GeneralErrorCode.USER_NOT_FOUND));
    }

    @Transactional
    public void saveOnBoarding(User user, String name, String companyType, String jobRole) {
        user.update(name);

        CareerPreference pref = careerPreferenceRepository.findByUser(user)
                .orElseGet(() -> CareerPreference.builder()
                        .user(user)
                        .build());

        pref.update(companyType, jobRole);
        careerPreferenceRepository.save(pref);
    }

    @Transactional(readOnly = true)
    public CareerPreference getCareerPreference(User user) {
        return careerPreferenceRepository.findByUser(user)
                .orElseThrow(() -> new BusinessException(GeneralErrorCode.USER_HAVE_NO_PREFERENCE));
    }
}
