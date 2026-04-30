package com.certifolio.server.domain.community.service;

import com.certifolio.server.domain.community.dto.request.PostRequestDTO;
import com.certifolio.server.domain.community.dto.response.CommentResponseDTO;
import com.certifolio.server.domain.community.dto.response.PostListResponseDTO;
import com.certifolio.server.domain.community.dto.response.PostResponseDTO;
import com.certifolio.server.domain.community.entity.Post;
import com.certifolio.server.domain.community.entity.PostType;
import com.certifolio.server.domain.community.repository.CommentRepository;
import com.certifolio.server.domain.community.repository.PostRepository;
import com.certifolio.server.domain.user.entity.User;
import com.certifolio.server.domain.user.service.UserService;
import com.certifolio.server.global.apiPayload.code.GeneralErrorCode;
import com.certifolio.server.global.apiPayload.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class PostService {

    private final UserService userService;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    // 글 작성 - id만 반환
    @Transactional
    public Long createPost(Long userId, PostRequestDTO request) {
        User user = userService.getUserById(userId);
        Post post = Post.builder()
                .user(user)
                .title(request.title())
                .content(request.content())
                .type(request.type())
                .build();

        return postRepository.save(post).getId();
    }

    // 글 목록 조회 - type 필터 (null이면 전체)
    @Transactional(readOnly = true)
    public List<PostListResponseDTO> getPosts(PostType type) {
        return postRepository.findPostList(type);
    }

    // 글 상세 조회 - 댓글 목록 포함, 조회수 증가
    @Transactional
    public PostResponseDTO getPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(GeneralErrorCode.POST_NOT_FOUND));

        postRepository.increaseViewCount(postId);

        List<CommentResponseDTO> comments = commentRepository.findByPostWithUser(post)
                .stream()
                .map(CommentResponseDTO::from)
                .toList();
        return PostResponseDTO.from(post, comments);
    }

    // 글 수정 - 수정 후 프론트에서 상세 조회 API 재호출 상정
    @Transactional
    public void modifyPost(Long userId, Long postId, PostRequestDTO request) {
        Post post = getPostWithOwnerCheck(userId, postId);
        post.update(request.title(), request.content(), request.type());
    }

    // 글 삭제
    @Transactional
    public void deletePost(Long userId, Long postId) {
        Post post = getPostWithOwnerCheck(userId, postId);
        postRepository.delete(post);
    }

    private Post getPostWithOwnerCheck(Long userId, Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new BusinessException(GeneralErrorCode.POST_NOT_FOUND));

        if (!post.getUser().getId().equals(userId)) {
            throw new BusinessException(GeneralErrorCode.POST_UNAUTHORIZED);
        }

        return post;
    }
}