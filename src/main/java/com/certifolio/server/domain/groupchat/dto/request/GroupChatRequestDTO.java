package com.certifolio.server.domain.groupchat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class GroupChatRequestDTO {

    public record CreateRoomRequest(
            @NotBlank @Size(max = 100) String chattingroomName,
            @Size(max = 500) String description
    ) {}

    public record SendRequest(
            @NotBlank @Size(max = 2000) String content
    ) {}
}
