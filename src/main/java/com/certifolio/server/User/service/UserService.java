package com.certifolio.server.User.service;

import com.certifolio.server.User.domain.User;
import com.certifolio.server.Form.Education.dto.EducationDTO;
import com.certifolio.server.Form.Career.dto.CareerDTO;
import com.certifolio.server.User.repository.UserRepository;
import com.certifolio.server.Form.Education.repository.EducationRepository;
import com.certifolio.server.Form.Career.repository.CareerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * User 도메인 서비스
 * User 관련 데이터 조회를 담당하여 다른 도메인에서 User 정보에 접근할 때 사용
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final EducationRepository educationRepository;
    private final CareerRepository careerRepository;

    /**
     * 사용자 ID로 조회
     */
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다. ID: " + userId));
    }

    /**
     * 사용자 ID로 조회 (Optional)
     */
    public Optional<User> findById(Long userId) {
        return userRepository.findById(userId);
    }

    /**
     * Provider와 ProviderId로 조회
     */
    public Optional<User> findByProviderAndProviderId(String provider, String providerId) {
        return userRepository.findByProviderAndProviderId(provider, providerId);
    }

    /**
     * 이메일로 조회
     */
    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    /**
     * 사용자 존재 여부 확인
     */
    public boolean existsById(Long userId) {
        return userRepository.existsById(userId);
    }

    /**
     * 사용자의 학력 정보 조회 (DTO 반환)
     */
    public List<EducationDTO> getEducationsByUserId(Long userId) {
        return educationRepository.findByUserId(userId)
                .stream()
                .map(EducationDTO::from)
                .collect(Collectors.toList());
    }

    /**
     * 사용자의 경력 정보 조회 (DTO 반환)
     */
    public List<CareerDTO> getCareersByUserId(Long userId) {
        return careerRepository.findByUserId(userId)
                .stream()
                .map(CareerDTO::from)
                .collect(Collectors.toList());
    }
}
