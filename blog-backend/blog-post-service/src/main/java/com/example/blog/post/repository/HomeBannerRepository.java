package com.example.blog.post.repository;

import com.example.blog.post.model.HomeBanner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface HomeBannerRepository extends JpaRepository<HomeBanner, Long> {

    List<HomeBanner> findAllByOrderBySortOrderAsc();
}
