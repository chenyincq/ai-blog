package com.example.blog.post.web;

import com.example.blog.post.model.HomeBanner;
import com.example.blog.post.repository.HomeBannerRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/home-banners")
@CrossOrigin(originPatterns = "*", allowCredentials = "true")
public class HomeBannerController {

    private final HomeBannerRepository repository;

    public HomeBannerController(HomeBannerRepository repository) {
        this.repository = repository;
    }

    @GetMapping
    public List<HomeBanner> list() {
        return repository.findAllByOrderBySortOrderAsc();
    }

    @GetMapping("/admin/all")
    public List<HomeBanner> listAllForAdmin() {
        return repository.findAllByOrderBySortOrderAsc();
    }

    @PostMapping("/admin")
    public ResponseEntity<HomeBanner> create(@Valid @RequestBody HomeBannerRequest req) {
        HomeBanner b = new HomeBanner();
        b.setImageUrl(req.getImageUrl());
        b.setTitle(req.getTitle());
        b.setSubtitle(req.getSubtitle());
        b.setSortOrder(req.getSortOrder() != null ? req.getSortOrder() : 0);
        return ResponseEntity.ok(repository.save(b));
    }

    @PutMapping("/admin/{id}")
    public ResponseEntity<HomeBanner> update(@PathVariable("id") Long id, @Valid @RequestBody HomeBannerRequest req) {
        return repository.findById(id)
                .map(b -> {
                    b.setImageUrl(req.getImageUrl());
                    b.setTitle(req.getTitle());
                    b.setSubtitle(req.getSubtitle());
                    if (req.getSortOrder() != null) b.setSortOrder(req.getSortOrder());
                    return ResponseEntity.ok(repository.save(b));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        if (!repository.existsById(id)) return ResponseEntity.notFound().build();
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    public static class HomeBannerRequest {
        @NotBlank
        private String imageUrl;
        private String title;
        private String subtitle;
        private Integer sortOrder;

        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
        public String getTitle() { return title; }
        public void setTitle(String title) { this.title = title; }
        public String getSubtitle() { return subtitle; }
        public void setSubtitle(String subtitle) { this.subtitle = subtitle; }
        public Integer getSortOrder() { return sortOrder; }
        public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    }
}
