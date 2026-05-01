package com.certifolio.server.domain.form.dto.response;

import com.certifolio.server.domain.form.activity.dto.response.ActivityResponseDTO;
import com.certifolio.server.domain.form.career.dto.response.CareerResponseDTO;
import com.certifolio.server.domain.form.certificate.dto.response.CertificateResponseDTO;
import com.certifolio.server.domain.form.education.dto.response.EducationResponseDTO;
import com.certifolio.server.domain.form.project.dto.response.ProjectResponseDTO;
import com.certifolio.server.domain.user.dto.response.UserResponseDTO;

import java.util.List;

public record SpecResponseDTO(
        UserResponseDTO user,
        List<ActivityResponseDTO> activity,
        List<CareerResponseDTO> career,
        List<CertificateResponseDTO> certificate,
        List<EducationResponseDTO> education,
        List<ProjectResponseDTO> project
) {}
