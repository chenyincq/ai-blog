-- 为已有 comments 表增加审核字段（评论需审核通过后才在前台显示）
-- 若已执行过请忽略报错 “Duplicate column name 'approved'”
USE blog_db;

ALTER TABLE comments
  ADD COLUMN approved TINYINT(1) NOT NULL DEFAULT 0 COMMENT '是否审核通过，0=待审核 1=已通过' AFTER created_at;

-- 若希望历史评论默认显示，可执行： UPDATE comments SET approved = 1 WHERE approved = 0;
