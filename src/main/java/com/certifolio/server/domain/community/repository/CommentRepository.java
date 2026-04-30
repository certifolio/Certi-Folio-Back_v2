package com.certifolio.server.domain.community.repository;

import com.certifolio.server.domain.community.entity.Comment;
import com.certifolio.server.domain.community.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    @Query("SELECT c FROM Comment c JOIN FETCH c.user WHERE c.post = :post ORDER BY c.createdAt ASC")
    List<Comment> findByPostWithUser(@Param("post") Post post);

    long countByPost(Post post);


}
