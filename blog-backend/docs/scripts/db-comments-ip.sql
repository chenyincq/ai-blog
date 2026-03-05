-- 为 comments 表增加 ip 字段，保存评论/回复用户 IP（不对外显示）
USE blog_db;

ALTER TABLE comments
  ADD COLUMN ip VARCHAR(64) NULL COMMENT '评论者 IP，不对外展示' AFTER parent_id;
