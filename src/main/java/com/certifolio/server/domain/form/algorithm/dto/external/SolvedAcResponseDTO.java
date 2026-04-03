package com.certifolio.server.domain.form.algorithm.dto.external;


public record SolvedAcResponseDTO(
        String handle,
        Integer tier,
        Integer rating,
        Integer solvedCount
) {}
