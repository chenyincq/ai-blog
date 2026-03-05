package com.example.blog.post.model;

import com.example.blog.post.web.JsonViews;
import com.fasterxml.jackson.annotation.JsonView;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonView(JsonViews.PublicView.class)
    private Long id;

    @Column(name = "post_id", nullable = false)
    @JsonView(JsonViews.PublicView.class)
    private Long postId;

    @Column(name = "parent_id")
    @JsonView(JsonViews.PublicView.class)
    private Long parentId;

    @JsonView(JsonViews.AdminView.class)
    @Column(name = "ip", length = 64)
    private String ip;

    @JsonView(JsonViews.PublicView.class)
    @Column(name = "address", length = 128)
    private String address;

    @Column(nullable = false, length = 100)
    @JsonView(JsonViews.PublicView.class)
    private String author;

    @Lob
    @Column(nullable = false)
    @JsonView(JsonViews.PublicView.class)
    private String content;

    @Column(name = "created_at", nullable = false, updatable = false)
    @JsonView(JsonViews.PublicView.class)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @JsonView(JsonViews.PublicView.class)
    private Boolean approved = false;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getPostId() { return postId; }
    public void setPostId(Long postId) { this.postId = postId; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public String getIp() { return ip; }
    public void setIp(String ip) { this.ip = ip; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getAuthor() { return author; }
    public void setAuthor(String author) { this.author = author; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Boolean getApproved() { return approved; }
    public void setApproved(Boolean approved) { this.approved = approved; }
}
