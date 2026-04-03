package com.certifolio.server.domain.form.algorithm.service;

import com.certifolio.server.domain.form.algorithm.dto.external.SolvedAcResponseDTO;
import com.certifolio.server.domain.form.algorithm.dto.request.AlgorithmRequestDTO;
import com.certifolio.server.domain.form.algorithm.dto.response.AlgorithmResponseDTO;
import com.certifolio.server.domain.form.algorithm.entity.Algorithm;
import com.certifolio.server.domain.form.algorithm.repository.AlgorithmRepository;
import com.certifolio.server.domain.user.entity.User;
import com.certifolio.server.domain.user.service.UserService;
import com.certifolio.server.global.apiPayload.code.GeneralErrorCode;
import com.certifolio.server.global.apiPayload.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AlgorithmService {

    private final UserService userService;
    private final AlgorithmRepository algorithmRepository;
    private final WebClient webClient;



    // 알고리즘 정보 조회
    public AlgorithmResponseDTO getAlgorithm(Long userId) {
        Algorithm algorithm = algorithmRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(GeneralErrorCode.ALGORITHM_NOT_FOUND));
        return AlgorithmResponseDTO.from(algorithm);
    }

    // 핸들 등록 및 데이터 저장
    @Transactional
    public AlgorithmResponseDTO saveAlgorithm(Long userId, AlgorithmRequestDTO request) {
        User user = userService.getUserById(userId);
        SolvedAcResponseDTO solvedAcData = fetchSolvedAcData(request.bojHandle());

        Algorithm algorithm = algorithmRepository.findByUserId(userId)
                .orElseGet(() -> Algorithm.builder()
                        .user(user)
                        .bojHandle(request.bojHandle())
                        .build());

        algorithm.update(
                solvedAcData.tier(),
                solvedAcData.solvedCount(),
                solvedAcData.rating()
        );

        return AlgorithmResponseDTO.from(algorithmRepository.save(algorithm));
    }

    // 기존 핸들로 데이터 갱신
    @Transactional
    public AlgorithmResponseDTO syncAlgorithm(Long userId) {
        Algorithm algorithm = algorithmRepository.findByUserId(userId)
                .orElseThrow(() -> new BusinessException(GeneralErrorCode.ALGORITHM_NOT_FOUND));

        SolvedAcResponseDTO solvedAcData = fetchSolvedAcData(algorithm.getBojHandle());

        algorithm.update(
                solvedAcData.tier(),
                solvedAcData.solvedCount(),
                solvedAcData.rating()
        );

        return AlgorithmResponseDTO.from(algorithm);
    }

    private SolvedAcResponseDTO fetchSolvedAcData(String handle) {
        try {
            return webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("https")
                            .host("solved.ac")
                            .path("/api/v3/user/show")
                            .queryParam("handle", handle)
                            .build())
                    .retrieve()
                    .bodyToMono(SolvedAcResponseDTO.class)
                    .block();
        } catch (WebClientResponseException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                throw new BusinessException(GeneralErrorCode.ALGORITHM_HANDLE_NOT_FOUND);
            }
            throw new BusinessException(GeneralErrorCode.ALGORITHM_API_ERROR);
        } catch (Exception e) {
            throw new BusinessException(GeneralErrorCode.ALGORITHM_API_ERROR);
        }
    }
}
