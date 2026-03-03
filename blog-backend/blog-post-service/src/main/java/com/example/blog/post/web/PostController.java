package com.example.blog.post.web;

import com.example.blog.post.config.UploadProperties;
import com.example.blog.post.model.Comment;
import com.example.blog.post.model.Post;
import com.example.blog.post.model.PostLike;
import com.example.blog.post.repository.CommentRepository;
import com.example.blog.post.repository.PostLikeRepository;
import com.example.blog.post.repository.PostRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:5174", "http://localhost:5175", "http://127.0.0.1:5173", "http://127.0.0.1:5174", "http://127.0.0.1:5175"}, allowCredentials = "true")
public class PostController {

    private final PostRepository repository;
    private final CommentRepository commentRepository;
    private final PostLikeRepository postLikeRepository;
    private final UploadProperties uploadProperties;

    public PostController(PostRepository repository, CommentRepository commentRepository,
                         PostLikeRepository postLikeRepository, UploadProperties uploadProperties) {
        this.repository = repository;
        this.commentRepository = commentRepository;
        this.postLikeRepository = postLikeRepository;
        this.uploadProperties = uploadProperties;
    }

    @GetMapping
    public Map<String, Object> listPublished(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size,
            @RequestParam(name = "author", required = false) String author,
            @RequestParam(name = "category", required = false) String category,
            @RequestParam(name = "tag", required = false) String tag) {
        String af = trim(author);
        String cf = trim(category);
        String tf = trim(tag);
        if (af == null && cf == null && tf == null) {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<Post> p = repository.findByPublishedTrue(pageable);
            return pageBody(p.getContent(), p.getTotalElements(), p.getNumber(), p.getSize());
        }
        List<Post> all = repository.findByPublishedTrueOrderByCreatedAtDesc();
        List<Post> filtered = all.stream()
                .filter(post -> af == null || containsIgnoreCase(post.getAuthor(), af))
                .filter(post -> cf == null || containsIgnoreCase(post.getCategory(), cf))
                .filter(post -> {
                    if (tf == null) return true;
                    if (post.getTags() == null) return false;
                    return Arrays.stream(post.getTags().split(","))
                            .map(String::trim)
                            .anyMatch(t -> containsIgnoreCase(t, tf));
                })
                .collect(Collectors.toList());
        long total = filtered.size();
        int from = Math.min(page * size, filtered.size());
        int to = Math.min(from + size, filtered.size());
        List<Post> content = from >= to ? new ArrayList<>() : filtered.subList(from, to);
        return pageBody(content, total, page, size);
    }

    /** 热门文章：按浏览量倒序取前10篇 */
    @GetMapping("/popular")
    public Map<String, Object> listPopular(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(
                Sort.Order.desc("viewCount"),
                Sort.Order.desc("createdAt")));
        Page<Post> p = repository.findByPublishedTrueOrderByViewCountDesc(pageable);
        return pageBody(p.getContent(), p.getTotalElements(), p.getNumber(), p.getSize());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Post> detail(@PathVariable("id") Long id) {
        return repository.findById(id)
                .filter(Post::getPublished)
                .map(post -> {
                    post.setViewCount((post.getViewCount() == null ? 0L : post.getViewCount()) + 1);
                    return ResponseEntity.ok(repository.save(post));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}/like/status")
    public ResponseEntity<Map<String, Object>> likeStatus(@PathVariable("id") Long id, @RequestParam("visitorId") String visitorId) {
        if (!repository.existsById(id)) return ResponseEntity.notFound().build();
        boolean liked = postLikeRepository.existsByPostIdAndVisitorId(id, visitorId);
        return ResponseEntity.ok(Map.of("liked", liked));
    }

    @PostMapping("/{id}/like")
    public ResponseEntity<Map<String, Object>> addLike(@PathVariable("id") Long id, @RequestBody Map<String, String> body) {
        String visitorId = body != null ? body.get("visitorId") : null;
        if (visitorId == null || visitorId.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return repository.findById(id)
                .filter(Post::getPublished)
                .map(post -> {
                    if (postLikeRepository.existsByPostIdAndVisitorId(id, visitorId)) {
                        return ResponseEntity.ok(Map.<String, Object>of(
                                "likeCount", post.getLikeCount(),
                                "liked", true));
                    }
                    PostLike like = new PostLike();
                    like.setPostId(id);
                    like.setVisitorId(visitorId);
                    postLikeRepository.save(like);
                    post.setLikeCount((post.getLikeCount() == null ? 0L : post.getLikeCount()) + 1);
                    repository.save(post);
                    return ResponseEntity.ok(Map.<String, Object>of(
                            "likeCount", post.getLikeCount(),
                            "liked", true));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}/like")
    public ResponseEntity<Map<String, Object>> removeLike(@PathVariable("id") Long id, @RequestParam("visitorId") String visitorId) {
        if (visitorId == null || visitorId.isBlank()) {
            return ResponseEntity.badRequest().build();
        }
        return repository.findById(id)
                .filter(Post::getPublished)
                .map(post -> {
                    postLikeRepository.findByPostIdAndVisitorId(id, visitorId).ifPresent(postLikeRepository::delete);
                    long current = post.getLikeCount() == null ? 0L : post.getLikeCount();
                    if (current > 0) {
                        post.setLikeCount(current - 1);
                        repository.save(post);
                    }
                    return ResponseEntity.ok(Map.<String, Object>of(
                            "likeCount", post.getLikeCount(),
                            "liked", false));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/tags")
    public List<Map<String, Object>> tagCloud() {
        List<Post> posts = repository.findByPublishedTrueOrderByCreatedAtDesc();
        Map<String, Long> count = new HashMap<>();
        for (Post p : posts) {
            if (p.getTags() == null) continue;
            for (String t : p.getTags().split(",")) {
                String s = trim(t);
                if (s != null && !s.isEmpty()) count.put(s, count.getOrDefault(s, 0L) + 1);
            }
        }
        return count.entrySet().stream()
                .map(e -> Map.<String, Object>of("name", e.getKey(), "count", e.getValue()))
                .sorted((a, b) -> Long.compare((Long) b.get("count"), (Long) a.get("count")))
                .collect(Collectors.toList());
    }

    @GetMapping("/categories")
    public List<Map<String, Object>> categories() {
        List<Post> posts = repository.findByPublishedTrueOrderByCreatedAtDesc();
        Map<String, Long> count = new HashMap<>();
        for (Post p : posts) {
            String c = trim(p.getCategory());
            if (c != null && !c.isEmpty()) count.put(c, count.getOrDefault(c, 0L) + 1);
        }
        return count.entrySet().stream()
                .map(e -> Map.<String, Object>of("name", e.getKey(), "count", e.getValue()))
                .sorted((a, b) -> Long.compare((Long) b.get("count"), (Long) a.get("count")))
                .collect(Collectors.toList());
    }

    @GetMapping("/admin/all")
    public Map<String, Object> listAllForAdmin(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Post> p = repository.findAll(pageable);
        return pageBody(p.getContent(), p.getTotalElements(), p.getNumber(), p.getSize());
    }

    @PostMapping("/admin")
    public ResponseEntity<Post> create(@Valid @RequestBody PostRequest req) {
        Post post = mapToPost(new Post(), req);
        return ResponseEntity.ok(repository.save(post));
    }

    @PutMapping("/admin/{id}")
    public ResponseEntity<Post> update(@PathVariable("id") Long id, @Valid @RequestBody PostRequest req) {
        return repository.findById(id)
                .map(p -> ResponseEntity.ok(repository.save(mapToPost(p, req))))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/admin/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") Long id) {
        if (!repository.existsById(id)) return ResponseEntity.notFound().build();
        repository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/admin/cover")
    public Map<String, String> uploadCover(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) throw new IOException("Empty file");
        Path dir = uploadProperties.resolveAbsolutePath();
        Files.createDirectories(dir);
        String ext = "";
        String name = file.getOriginalFilename();
        if (name != null && name.contains(".")) ext = name.substring(name.lastIndexOf('.'));
        String filename = "cover_" + System.currentTimeMillis() + ext;
        file.transferTo(dir.resolve(filename).toFile());
        String url = ServletUriComponentsBuilder.fromCurrentContextPath().path("/uploads/").path(filename).toUriString();
        return Map.of("url", url);
    }

    @PostMapping("/admin/image")
    public Map<String, String> uploadImage(@RequestParam("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) throw new IOException("Empty file");
        Path dir = uploadProperties.resolveAbsolutePath();
        Files.createDirectories(dir);
        String ext = "";
        String name = file.getOriginalFilename();
        if (name != null && name.contains(".")) ext = name.substring(name.lastIndexOf('.'));
        String filename = "image_" + System.currentTimeMillis() + ext;
        file.transferTo(dir.resolve(filename).toFile());
        return Map.of("url", "/uploads/" + filename);
    }

    @GetMapping("/admin/comments")
    public Page<Comment> listCommentsAdmin(
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size,
            @RequestParam(name = "postId", required = false) Long postId) {
        Pageable pageable = PageRequest.of(page, size);
        if (postId != null) {
            return commentRepository.findByPostIdOrderByCreatedAtDesc(postId, pageable);
        }
        return commentRepository.findAllByOrderByCreatedAtDesc(pageable);
    }

    @DeleteMapping("/admin/comments/{commentId}")
    public ResponseEntity<Void> deleteCommentAdmin(@PathVariable("commentId") Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            return ResponseEntity.notFound().build();
        }
        commentRepository.deleteById(commentId);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/admin/comments/{commentId}/approve")
    public ResponseEntity<Comment> approveCommentAdmin(@PathVariable("commentId") Long commentId) {
        return commentRepository.findById(commentId)
                .map(c -> {
                    c.setApproved(true);
                    return ResponseEntity.ok(commentRepository.save(c));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{postId}/comments")
    public List<Comment> listComments(@PathVariable("postId") Long postId) {
        return commentRepository.findByPostIdAndApprovedOrderByCreatedAtAsc(postId, true);
    }

    @PostMapping("/{postId}/comments")
    public ResponseEntity<Comment> addComment(@PathVariable("postId") Long postId, @Valid @RequestBody CommentRequest req) {
        if (!repository.existsById(postId)) {
            return ResponseEntity.notFound().build();
        }
        Comment c = new Comment();
        c.setPostId(postId);
        c.setAuthor(req.getAuthor());
        c.setContent(req.getContent());
        c.setApproved(false);
        return ResponseEntity.ok(commentRepository.save(c));
    }

    private Post mapToPost(Post p, PostRequest req) {
        p.setTitle(req.getTitle());
        p.setSummary(req.getSummary());
        p.setContent(req.getContent());
        p.setAuthor(req.getAuthor());
        p.setCategory(req.getCategory());
        p.setTags(req.getTags());
        p.setCoverUrl(req.getCoverUrl());
        p.setPublished(Boolean.TRUE.equals(req.getPublished()));
        return p;
    }

    private Map<String, Object> pageBody(List<Post> content, long total, int page, int size) {
        Map<String, Object> m = new HashMap<>();
        m.put("content", content);
        m.put("totalElements", total);
        m.put("page", page);
        m.put("size", size);
        return m;
    }

    private String trim(String s) {
        if (s == null) return null;
        String t = s.trim();
        return t.isEmpty() ? null : t;
    }

    private boolean containsIgnoreCase(String value, String filter) {
        return value != null && value.toLowerCase(Locale.ROOT).contains(filter.toLowerCase(Locale.ROOT));
    }

    public static class CommentRequest {
        @NotBlank
        private String author;
        @NotBlank
        private String content;
        public String getAuthor() { return author; }
        public void setAuthor(String author) { this.author = author; }
        public String getContent() { return content; }
        public void setContent(String content) { this.content = content; }
    }
}
