package com.certifolio.server.domain.mentoring.dto.request;

import com.certifolio.server.domain.mentoring.entity.PreferredFormat;
import com.certifolio.server.domain.mentoring.entity.SlotType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;

public class MentorRequestDTO {

    public record MentorApplicationRequest(
            @NotBlank String name,
            @NotBlank String title,
            @NotBlank String company,
            @NotBlank String experience,
            @NotNull @NotEmpty List<String> expertise,
            @NotBlank String bio,
            @Valid @NotNull @NotEmpty List<AvailabilityRequest> availability,
            @NotNull PreferredFormat preferredFormat,
            @NotNull @NotEmpty List<String> certificates
    ) {}

    public record AvailabilityRequest(
            @NotNull DayOfWeek dayOfWeek,
            @NotNull LocalTime startTime,
            @NotNull LocalTime endTime,
            @NotNull SlotType slotType
    ) {}
}
