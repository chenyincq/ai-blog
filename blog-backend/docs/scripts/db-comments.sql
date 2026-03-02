-- 若已存在 blog_db 且无 comments 表，可只执行本脚本
USE blog_db;

CREATE TABLE IF NOT EXISTS comments (
  id         BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id    BIGINT NOT NULL,
  author     VARCHAR(100) NOT NULL,
  content    TEXT         NOT NULL,
  created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  KEY idx_post_id (post_id)
);
