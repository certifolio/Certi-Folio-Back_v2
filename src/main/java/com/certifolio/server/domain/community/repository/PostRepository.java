package com.certifolio.server.domain.community.repository;

import com.certifolio.server.domain.community.entity.Post;
import com.certifolio.server.domain.community.entity.PostType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    List<Post> findByType(PostType type);
}
