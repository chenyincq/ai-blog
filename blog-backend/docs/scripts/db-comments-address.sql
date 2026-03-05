-- 为 comments 表增加 address 字段，存储根据 IP 解析的地理位置
USE blog_db;

ALTER TABLE comments
  ADD COLUMN address VARCHAR(128) NULL COMMENT '根据IP解析的地址' AFTER ip;
