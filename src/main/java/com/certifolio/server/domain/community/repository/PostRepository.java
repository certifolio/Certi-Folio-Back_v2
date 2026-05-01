package com.certifolio.server.domain.community.repository;

import com.certifolio.server.domain.community.dto.response.PostListResponseDTO;
import com.certifolio.server.domain.community.entity.Post;
import com.certifolio.server.domain.community.entity.PostType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {
    @Query("""
        SELECT new com.certifolio.server.domain.community.dto.response.PostListResponseDTO(
            p.id, p.user.name, p.title, p.type, p.viewCount, COUNT(c), p.createdAt
        )
        FROM Post p
        LEFT JOIN Comment c ON c.post = p
        WHERE (:type IS NULL OR p.type = :type)
        GROUP BY p.id, p.user.name, p.title, p.type, p.viewCount, p.createdAt
        ORDER BY p.createdAt DESC
    """)
    List<PostListResponseDTO> findPostList(@Param("type") PostType type);
}
