package com.certifolio.server.domain.user.dto.response;

import com.certifolio.server.domain.user.entity.Role;
import com.certifolio.server.domain.user.entity.User;
import com.certifolio.server.domain.user.entity.CareerPreference;

public record UserResponseDTO(
        Long id,
        String name,
        String email,
        String picture,
        Role role,
        String provider,
        String companyType,
        String jobRole
) {
    public static UserResponseDTO from(User user) {
        return from(user, null);
    }

    public static UserResponseDTO from(User user, CareerPreference preference) {
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPicture(),
                user.getRole(),
                user.getProvider(),
                preference != null ? preference.getCompanyType() : null,
                preference != null ? preference.getJobRole() : null
        );
    }
}
