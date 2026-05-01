package com.certifolio.server.domain.portfolio.dto.request;

import java.util.Map;

public record PortfolioDraftUpdateRequest(
        Map<String, Object> content
) {}
