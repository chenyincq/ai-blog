package com.example.blog.post.repository;

import com.example.blog.post.model.PostLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    boolean existsByPostIdAndVisitorId(Long postId, String visitorId);

    Optional<PostLike> findByPostIdAndVisitorId(Long postId, String visitorId);
}
