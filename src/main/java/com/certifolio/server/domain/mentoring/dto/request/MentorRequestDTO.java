package com.certifolio.server.domain.mentoring.dto.request;

import com.certifolio.server.domain.mentoring.entity.PreferredFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class MentorRequestDTO {

    public record MentorApplicationRequest(
            @NotBlank String name,
            @NotBlank String title,
            @NotBlank String company,
            @NotBlank String experience,
            @NotNull @NotEmpty List<String> expertise,
            @NotBlank String bio,
            @NotNull @NotEmpty List<String> availability,
            @NotNull PreferredFormat preferredFormat,
            @NotNull @NotEmpty List<String> certificates
    ) {}
}
