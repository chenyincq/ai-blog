-- 为 comments 表增加 parent_id 字段，支持回复功能（回复同样需审核）
-- 若已执行过请忽略
USE blog_db;

ALTER TABLE comments
  ADD COLUMN parent_id BIGINT NULL COMMENT '父评论 ID，NULL 表示顶层评论' AFTER post_id,
  ADD KEY idx_parent_id (parent_id);
