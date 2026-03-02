package com.example.blog.post.web;

import com.example.blog.post.model.FriendLink;
import com.example.blog.post.repository.FriendLinkRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/friend-links")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175", "http://127.0.0.1:5173", "http://127.0.0.1:5174", "http://127.0.0.1:5175"}, allowCredentials = "true")
public class FriendLinkController {

    private final FriendLinkRepository repository;

    public FriendLinkController(FriendLinkRepository repository) {
        this.repository = repository;
    }

    /** 前台：获取全部友情链接（按 sort_order 升序） */
    @GetMapping
    public List<FriendLink> list() {
        return repository.findAllByOrderBySortOrderAsc();
    }

    /** 管理端：获取全部 */
    @GetMapping("/admin/all")
    public List<FriendLink> listAllForAdmin() {
        return repository.findAllByOrderBySortOrderAsc();
    }

    /** 管理端：新增 */
    @PostMapping("/admin")
    public ResponseEntity<FriendLink> create(@Valid @RequestBody FriendLinkRequest req) {
        FriendLink link = new FriendLink();
        link.setName(req.getName());
        link.setUrl(req.getUrl());
        link.setDescription(req.getDescription());
        link.setSortOrder(req.getSortOrder() != null ? req.getSortOrder() : 0);
        return ResponseEntity.ok(repository.save(link));
    }

    /** 管理端：更新 */
    @PutMapping("/admin/{id}")
    public ResponseEntity<FriendLink> update(@PathVariable("id") Long id, @Valid @RequestBody FriendLinkRequest req) {
        return repository.findById(id)
                .map(link -> {
                    link.setName(req.getName());
                    link.setUrl(req.getUrl());
                    link.setDescription(req.getDescription());
                    if (req.getSortOrder() != null) link.setSortOrder(req.getSortOrder());
                    return ResponseEntity.ok(repository.save(link));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    /** 管理端：删除 */
    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        if (!repository.existsById(id)) return ResponseEntity.notFound().build();
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    public static class FriendLinkRequest {
        @NotBlank
        private String name;
        @NotBlank
        private String url;
        private String description;
        private Integer sortOrder;

        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public String getUrl() { return url; }
        public void setUrl(String url) { this.url = url; }
        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }
        public Integer getSortOrder() { return sortOrder; }
        public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    }
}
