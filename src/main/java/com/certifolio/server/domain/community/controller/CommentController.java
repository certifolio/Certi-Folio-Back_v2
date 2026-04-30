package com.certifolio.server.domain.community.controller;

import com.certifolio.server.domain.community.dto.request.CommentCreateRequestDTO;
import com.certifolio.server.domain.community.dto.request.CommentModifyRequestDTO;
import com.certifolio.server.domain.community.service.CommentService;
import com.certifolio.server.global.apiPayload.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    // 댓글 작성
    @PostMapping("/create")
    public ApiResponse<Long> createComment(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody CommentCreateRequestDTO request
    ) {
        Long commentId = commentService.createComment(userId, request);
        return ApiResponse.onSuccess("댓글 작성 성공", commentId);
    }

    // 댓글 수정
    @PatchMapping("/{commentId}")
    public ApiResponse<Void> modifyComment(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long commentId,
            @Valid @RequestBody CommentModifyRequestDTO request
    ) {
        commentService.modifyComment(userId, commentId, request);
        return ApiResponse.onSuccess("댓글 수정 성공");
    }

    // 댓글 삭제
    @DeleteMapping("/{commentId}")
    public ApiResponse<Void> deleteComment(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long commentId
    ) {
        commentService.deleteComment(userId, commentId);
        return ApiResponse.onSuccess("댓글 삭제 성공");
    }
}
