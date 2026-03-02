-- 为已有数据库增加浏览量、点赞字段及点赞表（仅需执行一次；若列已存在会报错，可忽略对应行）
USE blog_db;

ALTER TABLE posts ADD COLUMN view_count BIGINT NOT NULL DEFAULT 0;
ALTER TABLE posts ADD COLUMN like_count BIGINT NOT NULL DEFAULT 0;

CREATE TABLE IF NOT EXISTS post_likes (
  id         BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id    BIGINT NOT NULL,
  visitor_id VARCHAR(128) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_post_visitor (post_id, visitor_id),
  KEY idx_post_id (post_id)
);
