package com.example.blog.post.repository;

import com.example.blog.post.model.FriendLink;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FriendLinkRepository extends JpaRepository<FriendLink, Long> {

    List<FriendLink> findAllByOrderBySortOrderAsc();
}
