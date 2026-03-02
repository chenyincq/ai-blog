-- 首页轮播/背景图配置表（已有 blog_db 时执行）
USE blog_db;

CREATE TABLE IF NOT EXISTS home_banners (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  image_url   VARCHAR(500) NOT NULL COMMENT '背景图地址',
  title       VARCHAR(200) COMMENT '主标题',
  subtitle    VARCHAR(300) COMMENT '副标题',
  sort_order  INT NOT NULL DEFAULT 0 COMMENT '排序',
  created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO home_banners (image_url, title, subtitle, sort_order) VALUES
('https://images.unsplash.com/photo-1499750310107-5fef28a66643?w=1200&h=400&fit=crop', '欢迎来到我的博客', '记录 · 分享 · 交流', 10);
