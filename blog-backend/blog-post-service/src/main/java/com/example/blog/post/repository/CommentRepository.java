package com.example.blog.post.repository;

import com.example.blog.post.model.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByPostIdAndApprovedOrderByCreatedAtAsc(Long postId, Boolean approved);

    Page<Comment> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<Comment> findByPostIdOrderByCreatedAtDesc(Long postId, Pageable pageable);
}
