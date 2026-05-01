package com.certifolio.server.domain.community.controller;

import com.certifolio.server.domain.community.dto.request.PostRequestDTO;
import com.certifolio.server.domain.community.dto.response.PostListResponseDTO;
import com.certifolio.server.domain.community.dto.response.PostResponseDTO;
import com.certifolio.server.domain.community.entity.PostType;
import com.certifolio.server.domain.community.service.PostService;
import com.certifolio.server.global.apiPayload.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    // 글 작성
    @PostMapping("/create")
    public ApiResponse<Long> createPost(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody PostRequestDTO request
    ) {
        Long postId = postService.createPost(userId, request);
        return ApiResponse.onSuccess("글 생성 성공", postId);
    }

    // 글 상세 조회
    @GetMapping("/{postId}")
    public ApiResponse<PostResponseDTO> getPost(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postId
    ) {
        return ApiResponse.onSuccess("글 상세 조회 성공", postService.getPost(userId, postId));
    }

    // 글 수정
    @PatchMapping({"/{postId}"})
    public ApiResponse<Void> modifyPost(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postId,
            @Valid @RequestBody PostRequestDTO request
    ) {
        postService.modifyPost(userId, postId, request);
        return ApiResponse.onSuccess("글 수정 완료");
    }

    // 글 삭제
    @DeleteMapping("/{postId}")
    public ApiResponse<Void> deletePost(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postId
    ) {
        postService.deletePost(userId, postId);
        return ApiResponse.onSuccess("글 삭제 성공");
    }

    // 글 목록 조회
    @GetMapping
    public ApiResponse<List<PostListResponseDTO>> getPosts(
            @RequestParam(required = false) PostType type
    ) {
        List<PostListResponseDTO> posts = postService.getPosts(type);
        return ApiResponse.onSuccess("글 목록 조회 성공", posts);
    }
}
