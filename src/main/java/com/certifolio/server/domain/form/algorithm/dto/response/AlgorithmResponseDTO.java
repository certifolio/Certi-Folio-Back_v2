package com.certifolio.server.domain.form.algorithm.dto.response;

import com.certifolio.server.domain.form.algorithm.entity.Algorithm;

public record AlgorithmResponseDTO(
        String bojHandle,
        Integer tier,
        Integer rating,
        Integer solvedCount
) {
    public static AlgorithmResponseDTO from(Algorithm algorithm) {
        return new AlgorithmResponseDTO(
                algorithm.getBojHandle(),
                algorithm.getTier(),
                algorithm.getRating(),
                algorithm.getSolvedCount()
        );
    }
}
