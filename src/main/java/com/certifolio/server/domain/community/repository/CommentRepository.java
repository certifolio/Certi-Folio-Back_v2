package com.certifolio.server.domain.community.repository;

import com.certifolio.server.domain.community.entity.Comment;
import com.certifolio.server.domain.community.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByPost(Post post);
    long countByPost(Post post);
}
