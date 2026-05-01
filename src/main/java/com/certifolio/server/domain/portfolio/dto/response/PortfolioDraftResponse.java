package com.certifolio.server.domain.portfolio.dto.response;

import com.certifolio.server.domain.portfolio.entity.PortfolioDraft;
import com.certifolio.server.global.apiPayload.code.GeneralErrorCode;
import com.certifolio.server.global.apiPayload.exception.BusinessException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.util.Map;

public record PortfolioDraftResponse (
        Long id,
        String status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        Map<String, Object> content
) {
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static PortfolioDraftResponse from(PortfolioDraft draft) {
        try {
            Map<String, Object> content = objectMapper.readValue(
                    draft.getDraftContent(),
                    new TypeReference<>() {
                    }
            );

            return new PortfolioDraftResponse(
                    draft.getId(),
                    draft.getStatus().name(),
                    draft.getCreatedAt(),
                    draft.getUpdatedAt(),
                    content
            );
        } catch (JsonProcessingException e) {
            throw new BusinessException(GeneralErrorCode.JSON_PARSE_ERROR);
        }
    }
}
