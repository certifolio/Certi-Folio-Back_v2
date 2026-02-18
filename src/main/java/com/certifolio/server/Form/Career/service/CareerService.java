package com.certifolio.server.Form.Career.service;

import com.certifolio.server.User.domain.User;
import com.certifolio.server.User.repository.UserRepository;
import com.certifolio.server.Form.Career.domain.Career;
import com.certifolio.server.Form.Career.dto.CareerDTO;
import com.certifolio.server.Form.util.DateUtils;
import com.certifolio.server.Form.Career.repository.CareerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CareerService {

    private final UserRepository userRepository;
    private final CareerRepository careerRepository;

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    /**
     * 경력 전체 저장 (기존 삭제 후 재저장)
     */
    public void saveCareers(Long userId, List<CareerDTO> dtos) {
        User user = getUser(userId);

        // 기존 데이터 삭제
        careerRepository.findAllByUser(user).forEach(careerRepository::delete);

        if (dtos == null || dtos.isEmpty()) {
            return;
        }

        List<Career> careers = dtos.stream()
                .map(dto -> Career.builder()
                        .user(user)
                        .type(dto.getType())
                        .company(dto.getCompany())
                        .position(dto.getPosition())
                        .startDate(DateUtils.parseDate(dto.getStartDate()))
                        .endDate(DateUtils.parseDate(dto.getEndDate()))
                        .description(dto.getDescription())
                        .build())
                .collect(Collectors.toList());

        careerRepository.saveAll(careers);
    }

    /**
     * 경력 목록 조회
     */
    public List<CareerDTO> getCareers(Long userId) {
        User user = getUser(userId);
        return careerRepository.findAllByUser(user).stream()
                .map(CareerDTO::from)
                .collect(Collectors.toList());
    }

    public CareerDTO addCareer(Long userId, CareerDTO dto) {
        User user = getUser(userId);

        LocalDate startDate = DateUtils.parseDate(dto.getStartDate());
        LocalDate endDate = DateUtils.parseDate(dto.getEndDate());

        Career career = Career.builder()
                .user(user)
                .type(dto.getType())
                .company(dto.getCompany())
                .position(dto.getPosition())
                .startDate(startDate)
                .endDate(endDate)
                .description(dto.getDescription())
                .build();

        Career saved = careerRepository.save(career);

        return CareerDTO.from(saved);

    }

    public CareerDTO updateCareer(Long userId, Long careerId, CareerDTO dto) {
        Career career = careerRepository.findById(careerId)
                .orElseThrow(() -> new IllegalArgumentException("Career not found"));

        if (!career.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        LocalDate startDate = DateUtils.parseDate(dto.getStartDate());
        LocalDate endDate = DateUtils.parseDate(dto.getEndDate());

        career.update(dto.getType(), dto.getCompany(), dto.getPosition(), startDate, endDate, dto.getDescription());

        Career updated = careerRepository.save(career);

        return CareerDTO.from(updated);
    }

    public void deleteCareer(Long userId, Long careerId) {
        Career career = careerRepository.findById(careerId)
                .orElseThrow(() -> new IllegalArgumentException("Career not found"));

        if (!career.getUser().getId().equals(userId)) {
            throw new RuntimeException("Unauthorized");
        }

        careerRepository.delete(career);
    }

}
