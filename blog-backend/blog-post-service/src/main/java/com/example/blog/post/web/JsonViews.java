package com.example.blog.post.web;

/**
 * Jackson 视图：控制接口返回字段
 * - PublicView: 公开接口，不含 ip
 * - AdminView: 管理接口，含 ip
 */
public interface JsonViews {
    interface PublicView {}
    interface AdminView extends PublicView {}
}
