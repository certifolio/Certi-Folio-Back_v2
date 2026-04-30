package com.certifolio.server.global.dev;

import com.certifolio.server.global.apiPayload.response.ApiResponse;
import com.certifolio.server.global.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Profile("local")
@RestController
@RequiredArgsConstructor
@RequestMapping("/dev")
public class DevTokenController {

    private final TokenProvider tokenProvider;

    @GetMapping("/token")
    public ApiResponse<String> getTestToken(@RequestParam Long userId) {
        String token = tokenProvider.createAccessToken(userId, "USER");
        return ApiResponse.onSuccess("테스트 토큰 발급 성공", token);
    }
}
