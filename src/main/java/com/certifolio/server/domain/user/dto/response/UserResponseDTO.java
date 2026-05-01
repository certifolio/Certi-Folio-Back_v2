package com.certifolio.server.domain.user.dto.response;

import com.certifolio.server.domain.user.entity.Role;
import com.certifolio.server.domain.user.entity.User;

public record UserResponseDTO(
        Long id,
        String name,
        String email,
        String picture,
        Role role,
        String provider
) {
    public static UserResponseDTO from(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPicture(),
                user.getRole(),
                user.getProvider()
        );
    }
}