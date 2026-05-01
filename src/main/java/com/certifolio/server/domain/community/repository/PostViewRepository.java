package com.certifolio.server.domain.community.repository;

import com.certifolio.server.domain.community.entity.PostView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PostViewRepository extends JpaRepository<PostView, Long> {

    @Modifying
    @Query(value = """
        INSERT IGNORE INTO post_views (post_id, user_id, created_at, updated_at)
        VALUES (:postId, :userId, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
    """, nativeQuery = true)
    int insertIgnore(@Param("postId") Long postId, @Param("userId") Long userId);
}
