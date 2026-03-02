-- 若已存在 blog_db 且无 friend_links 表，可只执行本脚本
USE blog_db;

CREATE TABLE IF NOT EXISTS friend_links (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  name        VARCHAR(100) NOT NULL COMMENT '链接名称',
  url         VARCHAR(500) NOT NULL COMMENT '链接地址',
  description VARCHAR(255) COMMENT '简要描述',
  sort_order  INT NOT NULL DEFAULT 0 COMMENT '排序，数字越小越靠前',
  created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO friend_links (name, url, description, sort_order) VALUES
('Vue 官网', 'https://vuejs.org', 'Vue 官方文档', 10),
('Spring 官网', 'https://spring.io', 'Spring 官方', 20),
('MDN', 'https://developer.mozilla.org', 'Web 技术文档', 30);
