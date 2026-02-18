package com.certifolio.server.Form.CodingTest.service;

import com.certifolio.server.Form.CodingTest.domain.CodingTest;
import com.certifolio.server.Form.CodingTest.dto.CodingTestDTO;
import com.certifolio.server.Form.CodingTest.dto.SolvedAcResponseDTO;
import com.certifolio.server.Form.CodingTest.repository.CodingTestRepository;
import com.certifolio.server.User.domain.User;
import com.certifolio.server.User.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CodingTestService {

    private final CodingTestRepository codingTestRepository;
    private final UserRepository userRepository;
    private final RestTemplate restTemplate = new RestTemplate(); // Simple instantiation

    private static final String SOLVED_AC_API_URL = "https://solved.ac/api/v3/user/show?handle=";

    /**
     * Fetch solved.ac data for a handle (Preview)
     */
    public SolvedAcResponseDTO fetchSolvedAcData(String handle) {
        try {
            return restTemplate.getForObject(SOLVED_AC_API_URL + handle, SolvedAcResponseDTO.class);
        } catch (HttpClientErrorException e) {
            throw new IllegalArgumentException("User not found on solved.ac: " + handle);
        } catch (Exception e) {
            throw new RuntimeException("Failed to fetch data from solved.ac: " + e.getMessage());
        }
    }

    /**
     * Get validated CodingTest data for a user
     */
    public CodingTestDTO getCodingTest(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return codingTestRepository.findByUser(user)
                .map(CodingTestDTO::from)
                .orElse(null);
    }

    /**
     * Save/Update CodingTest data
     */
    @Transactional
    public void saveCodingTest(Long userId, String handle) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // 1. Verify handle again
        SolvedAcResponseDTO solvedAcData = fetchSolvedAcData(handle);

        // 2. Find existing or create new
        CodingTest codingTest = codingTestRepository.findByUser(user)
                .orElse(CodingTest.builder()
                        .user(user)
                        .bojHandle(handle)
                        .build());

        // 3. Update fields
        codingTest.update(
                solvedAcData.getTier(),
                solvedAcData.getSolvedCount(),
                solvedAcData.getRating(),
                solvedAcData.getMaxStreak(),
                solvedAcData.getRank(),
                solvedAcData.getBio(),
                solvedAcData.getUserClass()
        );

        codingTestRepository.save(codingTest);
    }
}
