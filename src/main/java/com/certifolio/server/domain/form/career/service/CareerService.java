package com.certifolio.server.domain.form.career.service;

import com.certifolio.server.domain.form.career.dto.request.CareerRequestDTO;
import com.certifolio.server.domain.form.career.dto.response.CareerResponseDTO;
import com.certifolio.server.domain.form.career.entity.Career;
import com.certifolio.server.domain.form.career.repository.CareerRepository;
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
public class CareerService {

    private final UserService userService;
    private final CareerRepository careerRepository;

    // 경력 전체 조회
    @Transactional(readOnly = true)
    public List<CareerResponseDTO> getCareers(Long userId) {
        return careerRepository.findAllByUserId(userId).stream()
                .map(CareerResponseDTO::from)
                .toList();
    }

    // 경력 전체 저장 (최초)
    @Transactional
    public void saveCareer(Long userId, List<CareerRequestDTO> request) {
        User user = userService.getUserById(userId);

        if (request == null || request.isEmpty()) {
            throw new BusinessException(GeneralErrorCode.CAREER_NOT_INPUTTED);
        }

        List<Career> careers = request.stream()
                .map(dto -> Career.builder()
                        .user(user)
                        .type(dto.type())
                        .company(dto.company())
                        .position(dto.position())
                        .startDate(DateUtils.parseDate(dto.startDate()))
                        .endDate(DateUtils.parseDate(dto.endDate()))
                        .description(dto.description())
                        .build())
                .toList();

        careerRepository.saveAll(careers);
    }

    // 경력 단건 조회
    @Transactional(readOnly = true)
    public CareerResponseDTO getCareer(Long userId, Long careerId) {
        Career career = getCareerWithOwnerCheck(userId, careerId);
        return CareerResponseDTO.from(career);
    }

    // 경력 단건 추가
    @Transactional
    public CareerResponseDTO addCareer(Long userId, CareerRequestDTO request) {
        User user = userService.getUserById(userId);
        Career career = Career.builder()
                .user(user)
                .type(request.type())
                .company(request.company())
                .position(request.position())
                .startDate(DateUtils.parseDate(request.startDate()))
                .endDate(DateUtils.parseDate(request.endDate()))
                .description(request.description())
                .build();
        return CareerResponseDTO.from(careerRepository.save(career));
    }

    // 경력 단건 수정
    @Transactional
    public CareerResponseDTO modifyCareer(Long userId, Long careerId, CareerRequestDTO request) {
        Career career = getCareerWithOwnerCheck(userId, careerId);
        career.update(
                request.type(),
                request.company(),
                request.position(),
                DateUtils.parseDate(request.startDate()),
                DateUtils.parseDate(request.endDate()),
                request.description()
        );
        return CareerResponseDTO.from(career);
    }

    // 경력 단건 삭제
    @Transactional
    public void deleteCareer(Long userId, Long careerId) {
        Career career = getCareerWithOwnerCheck(userId, careerId);
        careerRepository.delete(career);
    }

    // 경력 확인
    private Career getCareerWithOwnerCheck(Long userId, Long careerId) {
        Career career = careerRepository.findById(careerId)
                .orElseThrow(() -> new BusinessException(GeneralErrorCode.CAREER_NOT_FOUND));
        if (!career.getUser().getId().equals(userId)) {
            throw new BusinessException(GeneralErrorCode.CAREER_UNAUTHORIZED);
        }
        return career;
    }
}
