package com.certifolio.server.domain.community.service;

import com.certifolio.server.domain.community.dto.request.CommentRequestDTO;
import com.certifolio.server.domain.community.entity.Comment;
import com.certifolio.server.domain.community.entity.Post;
import com.certifolio.server.domain.community.repository.CommentRepository;
import com.certifolio.server.domain.community.repository.PostRepository;
import com.certifolio.server.domain.user.entity.User;
import com.certifolio.server.domain.user.service.UserService;
import com.certifolio.server.global.apiPayload.code.GeneralErrorCode;
import com.certifolio.server.global.apiPayload.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final UserService userService;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    // 댓글 작성
    @Transactional
    public Long createComment(Long userId, CommentRequestDTO request) {
        User user = userService.getUserById(userId);
        Post post = postRepository.findById(request.postId())
                .orElseThrow(() -> new BusinessException(GeneralErrorCode.POST_NOT_FOUND));

        Comment comment = Comment.builder()
                .post(post)
                .user(user)
                .content(request.content())
                .build();

        return commentRepository.save(comment).getId();
    }

    // 댓글 수정
    @Transactional
    public void editComment(Long userId, Long commentId, CommentRequestDTO request) {
        Comment comment = getCommentWithOwnerCheck(userId, commentId);
        comment.update(request.content());
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(Long userId, Long commentId) {
        Comment comment = getCommentWithOwnerCheck(userId, commentId);
        commentRepository.delete(comment);
    }

    private Comment getCommentWithOwnerCheck(Long userId, Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(GeneralErrorCode.COMMENT_NOT_FOUND));
        if (!comment.getUser().getId().equals(userId)) {
            throw new BusinessException(GeneralErrorCode.COMMENT_UNAUTHORIZED);
        }
        return comment;
    }
}
