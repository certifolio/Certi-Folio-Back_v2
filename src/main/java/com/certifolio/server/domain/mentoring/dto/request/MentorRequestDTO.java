package com.certifolio.server.domain.mentoring.dto.request;

import java.util.List;

public class MentorRequestDTO {

    public record MentorApplicationRequest(
            String name,
            String title,
            String company,
            String experience,
            List<String> expertise,
            String bio,
            List<String> availability,
            String preferredFormat,
            List<String> certificates
    ) {}
}
