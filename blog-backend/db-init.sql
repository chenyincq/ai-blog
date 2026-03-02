-- 博客数据库初始化
-- 在 MySQL 中执行（可连接 192.168.100.183 后执行）
CREATE DATABASE IF NOT EXISTS blog_db DEFAULT CHARSET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE blog_db;

-- 用户表（账号密码登录 + 微信 unionid 绑定）
CREATE TABLE IF NOT EXISTS users (
  id           BIGINT PRIMARY KEY AUTO_INCREMENT,
  username     VARCHAR(50)   UNIQUE,
  password     VARCHAR(255),
  role         VARCHAR(20)   NOT NULL DEFAULT 'USER',
  wechat_openid VARCHAR(64)   UNIQUE COMMENT '微信 openid',
  wechat_unionid VARCHAR(64)  COMMENT '微信 unionid',
  nickname     VARCHAR(100)  COMMENT '微信昵称',
  avatar       VARCHAR(500)  COMMENT '头像 URL',
  created_at   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

-- 管理员账号（可选，也可仅用微信登录后在库中改 role）
INSERT INTO users (username, password, role) VALUES ('admin', 'admin123', 'ADMIN')
ON DUPLICATE KEY UPDATE username = VALUES(username);

-- 文章表
CREATE TABLE IF NOT EXISTS posts (
  id         BIGINT PRIMARY KEY AUTO_INCREMENT,
  title      VARCHAR(255) NOT NULL,
  summary    VARCHAR(500),
  content    TEXT         NOT NULL,
  author     VARCHAR(100) NOT NULL,
  category   VARCHAR(100),
  tags       VARCHAR(500),
  cover_url  VARCHAR(255),
  created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  published  TINYINT(1)  NOT NULL DEFAULT 1,
  view_count BIGINT       NOT NULL DEFAULT 0,
  like_count BIGINT       NOT NULL DEFAULT 0
);

-- 删除旧的文章与评论测试数据，便于重新生成
DELETE FROM comments;
DELETE FROM posts;

-- 初始化测试数据：带真实封面图与完整正文的示例文章
INSERT INTO posts (title, summary, content, author, category, tags, cover_url, published, view_count, like_count) VALUES
(
'欢迎来到我的博客',
'在这里记录技术学习、项目实践与日常思考，欢迎常来看看。',
'## 开篇的话\n\n你好，欢迎来到我的博客。这里是个人写作与分享的空间，主要会写三类内容：**技术笔记**、**项目实践**和**随想杂谈**。\n\n技术笔记会围绕前端、后端、数据库和运维等方向，把学习与踩坑过程整理成文，方便自己回顾，也希望能对读到的人有一点帮助。项目实践会记录一些小型项目从想法到实现的步骤，包括技术选型、接口设计和简单部署。随想杂谈则比较随意，可能是读书笔记、效率方法或对行业的一点看法。\n\n博客采用前后端分离架构：前端 Vue 3 + Vite，后端 Spring Boot 微服务，数据存储在 MySQL，支持日夜间主题切换和评论。后续会陆续补充更多功能与文章。\n\n感谢你的访问，期待与你交流。',
'管理员',
'随笔',
'随笔,介绍,博客',
'https://images.unsplash.com/photo-1499750310107-5fef28a66643?w=800&h=400&fit=crop',
1, 0, 0
),
(
'Vue 3 组合式 API 入门与实践',
'Vue 3 组合式 API 与 script setup 让逻辑复用和代码组织更清晰，本文介绍常用 API 与使用建议。',
'## 为什么需要组合式 API\n\nVue 2 的选项式 API 把数据、方法、生命周期分散在 data、methods、mounted 等选项中，单个逻辑（比如“用户信息 + 请求”）容易被拆到多处，复用要靠 mixin，容易产生命名冲突和来源不清。Vue 3 的组合式 API（Composition API）把同一逻辑收敛到一组 `ref`、`computed`、`watch` 和函数里，更利于按功能拆分和复用。\n\n## 核心 API 简介\n\n- **ref**：定义响应式基本类型，在脚本里用 `.value` 读写，模板中自动解包。\n- **reactive**：定义响应式对象，适合结构化数据。\n- **computed**：依赖其它响应式数据派生新值，带缓存。\n- **watch / watchEffect**：响应数据变化执行副作用，如请求、日志。\n- **onMounted / onUnmounted**：替代选项式里的 mounted、beforeUnmount 等生命周期。\n\n## script setup 写法\n\n在 `<script setup>` 中定义的变量、函数和 import 会直接暴露给模板，无需 return。组合式函数（composables）可以抽成独立文件，在多个组件中复用。\n\n```js\nimport { ref, computed, onMounted } from ''vue''\nconst count = ref(0)\nconst double = computed(() => count.value * 2)\nonMounted(() => console.log(''mounted''))\n```\n\n推荐把复杂页面的逻辑按“功能块”拆成多个 composable，再在组件里组合，这样可读性和可测试性都会更好。',
'管理员',
'前端',
'Vue,前端,JavaScript,组合式API',
'https://images.unsplash.com/photo-1633356122544-f134324a6cee?w=800&h=400&fit=crop',
1, 0, 0
),
(
'Spring Boot 快速搭建 REST 微服务',
'用 Spring Boot 在几分钟内搭一个可对外提供 REST 接口的微服务，并连接 MySQL 做持久化。',
'## Spring Boot 简介\n\nSpring Boot 在 Spring 生态上做了大量约定与自动配置，让开发者用很少的配置就能跑起一个 Web 应用或微服务。内嵌 Tomcat、默认配置、starter 依赖和 Actuator 健康检查等，都围绕“开箱即用”设计。\n\n## 创建项目\n\n推荐使用 [Spring Initializr](https://start.spring.io/) 或 IDE 的 Spring 向导，选择：\n\n- **Spring Web**：提供 Spring MVC 与内嵌 Tomcat，可暴露 REST 接口。\n- **Spring Data JPA**：简化对数据库的 CRUD 与 Repository 抽象。\n- **MySQL Driver**：连接 MySQL 数据库。\n\n生成项目后，在 `application.yml` 中配置数据源：\n\n```yaml\nspring:\n  datasource:\n    url: jdbc:mysql://localhost:3306/your_db\n    username: root\n    password: your_password\n  jpa:\n    hibernate:\n      ddl-auto: update\n```\n\n## 编写 REST 接口\n\n使用 `@RestController` 和 `@GetMapping` / `@PostMapping` 等注解即可定义接口；结合 `@RequestBody`、`@PathVariable` 接收参数。JPA 的 `JpaRepository` 提供 `findAll()`、`findById()`、`save()` 等方法，在 Service 层封装业务逻辑，在 Controller 层返回 JSON 即可。\n\n## 运行与扩展\n\n执行 `mvn spring-boot:run` 或运行主类的 main 方法即可启动。后续可增加全局异常处理、参数校验、JWT 鉴权等，逐步完善为一个可用的微服务。',
'管理员',
'后端',
'Java,Spring Boot,微服务,REST',
'https://images.unsplash.com/photo-1555066931-4365d14bab8c?w=800&h=400&fit=crop',
1, 0, 0
),
(
'MySQL 入门：连接、库表与常用命令',
'MySQL 日常开发中最常用的连接方式、库表操作与查询命令速览。',
'## 连接数据库\n\n在终端或命令行中执行：\n\n```bash\nmysql -h 主机 -u 用户名 -p\n```\n\n输入密码后进入 MySQL 客户端。`-h` 省略时默认本机。\n\n## 库与表操作\n\n- 查看所有库：`SHOW DATABASES;`\n- 创建库：`CREATE DATABASE blog_db DEFAULT CHARSET utf8mb4;`\n- 使用库：`USE blog_db;`\n- 查看当前库下所有表：`SHOW TABLES;`\n- 查看表结构：`DESC 表名;` 或 `SHOW CREATE TABLE 表名;`\n- 建表：使用 `CREATE TABLE` 定义列名、类型、主键、索引等。\n\n## 常用查询与更新\n\n- 查询：`SELECT 列 FROM 表 WHERE 条件 ORDER BY 列 LIMIT n;`\n- 插入：`INSERT INTO 表 (列1, 列2) VALUES (值1, 值2);`\n- 更新：`UPDATE 表 SET 列=值 WHERE 条件;`\n- 删除：`DELETE FROM 表 WHERE 条件;`\n\n## 建议\n\n生产环境务必避免在连接串中明文写密码；可配合环境变量或配置中心。建表时为常用查询条件加索引，并注意字符集（如 utf8mb4）以正确存储中文与 emoji。',
'管理员',
'数据库',
'MySQL,数据库,SQL',
'https://images.unsplash.com/photo-1544383835-bda2bc66a55d?w=800&h=400&fit=crop',
1, 0, 0
),
(
'深色模式与阅读体验',
'深色主题能减轻长时间阅读的用眼负担，搭配合适的对比度与字号可兼顾美观与可读性。',
'## 为什么选择深色模式\n\n在光线较暗的环境下，深色背景能减少屏幕对眼睛的刺激，不少开发者和读者习惯在夜间使用深色主题。对于以文字为主的博客，深色背景配合浅色文字，在 OLED 屏幕上还能节省电量。\n\n## 设计时要注意什么\n\n- **对比度**：文字与背景的对比度要符合可访问性标准（如 WCAG AA），避免灰字配深灰底导致难以辨认。\n- **层次**：用不同的灰度和边框区分正文、卡片、按钮等，避免“糊成一团”。\n- **强调色**：链接和按钮使用高亮色（如蓝、紫），在深色背景下保持醒目。\n- **图片**：部分图片在深色背景下会显得过亮，可适当降低亮度或加边框。\n\n## 实现方式\n\n前端可用 CSS 变量定义两套颜色（如 `--bg`、`--text`），通过切换根元素的 class（如 `theme-dark` / `theme-light`）或媒体查询 `prefers-color-scheme` 切换主题。把用户选择存到 localStorage，下次访问时优先应用用户偏好。\n\n兼顾日间与夜间两种模式，能让不同使用场景下的读者都获得舒适的阅读体验。',
'管理员',
'随笔',
'设计,前端,可访问性',
'https://images.unsplash.com/photo-1513542789411-b6d5b0592fd9?w=800&h=400&fit=crop',
1, 0, 0
),
(
'如何使用 Cursor 构建一个博客网站',
'介绍如何用 Cursor 从零搭建前后端分离的博客网站，包括环境准备、后端 Spring Boot、前端 Vue 3 与具体操作步骤。',
'## 一、Cursor 是什么\n\n**Cursor** 是一款基于 VS Code 的 AI 编程编辑器，内置大模型能力，可以在写代码时进行补全、解释、重构和按自然语言生成/修改代码。用它来构建博客的好处包括：快速搭架子、减少查文档、统一环境（前端+后端+SQL 都在同一项目）。\n\n## 二、环境准备\n\n需安装 Node.js 18+、Java 17+、Maven、MySQL 8+、Cursor。从 [cursor.com](https://cursor.com) 下载安装 Cursor。\n\n## 三、项目结构\n\n推荐前后端分离 + 微服务：前端 Vue 3 + Vite；后端 Spring Boot，可拆成博客文章服务、认证服务、评论/友情链接/轮播图等。\n\n## 四、后端搭建\n\n1. 用 Spring Initializr 创建项目，勾选 Spring Web、Spring Data JPA、MySQL Driver。\n2. 在 `application.yml` 配置数据源与 JPA，执行 `db-init.sql` 建表。\n3. 编写 Post 实体、JpaRepository、Controller：对外提供 `GET /api/posts`、`GET /api/posts/{id}`，管理端 `POST/PUT/DELETE /api/posts/admin`（需 JWT）。\n4. 认证服务：登录签发 JWT，管理端接口校验 Bearer token。\n\n## 五、前端搭建\n\n1. `npm create vite@latest blog-frontend -- --template vue`，安装 vue-router。\n2. 配置路由（首页、文章列表、详情、管理登录与文章管理）和 `api.js` 中的请求（getPosts、getPost、createPost、login 等）。\n3. 首页：轮播图 + 文章列表；文章详情：Markdown 转 HTML、评论；管理后台：token 存 localStorage，调用管理端 API。\n4. 用 CSS 变量做日夜间主题，偏好存 localStorage。\n\n## 六、使用 Cursor 的建议\n\n- 在聊天里写清需求并指定文件，让 AI 生成或改代码。\n- 用 @ 引用文件/文件夹，保持 API 与命名一致。\n- 分步实现：先列表与详情，再评论、后台、登录。\n- 把报错贴给 Cursor 做修改。\n- 建表与 SQL 可在 Cursor 中写或让 AI 生成后执行。\n\n## 七、运行与部署\n\n本地：启动 MySQL → 执行建表脚本 → 后端 `mvn spring-boot:run` → 前端 `npm run dev`。部署：前端 build 后放 Nginx/对象存储，后端打 jar 或 Docker，配置库地址与 JWT 等环境变量。\n\n按本文步骤在 Cursor 里一步步完成环境、接口、页面与主题，即可搭好博客架子并持续迭代。',
'管理员',
'随笔',
'Cursor,博客,Vue,Spring Boot,教程',
'https://images.unsplash.com/photo-1517694712202-14dd9538aa97?w=800&h=400&fit=crop',
1, 0, 0
);

-- 评论表
CREATE TABLE IF NOT EXISTS comments (
  id         BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id    BIGINT NOT NULL,
  author     VARCHAR(100) NOT NULL,
  content    TEXT         NOT NULL,
  created_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  approved   TINYINT(1)   NOT NULL DEFAULT 0 COMMENT '是否审核通过，0=待审核 1=已通过',
  KEY idx_post_id (post_id),
  KEY idx_approved (approved)
);

-- 文章点赞表（按访客 ID 去重，同一访客只能点赞/取消一次）
CREATE TABLE IF NOT EXISTS post_likes (
  id         BIGINT PRIMARY KEY AUTO_INCREMENT,
  post_id    BIGINT NOT NULL,
  visitor_id VARCHAR(128) NOT NULL,
  created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uk_post_visitor (post_id, visitor_id),
  KEY idx_post_id (post_id)
);

-- 友情链接表
CREATE TABLE IF NOT EXISTS friend_links (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  name        VARCHAR(100) NOT NULL COMMENT '链接名称',
  url         VARCHAR(500) NOT NULL COMMENT '链接地址',
  description VARCHAR(255) COMMENT '简要描述',
  sort_order  INT NOT NULL DEFAULT 0 COMMENT '排序，数字越小越靠前',
  created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 友情链接示例数据
INSERT INTO friend_links (name, url, description, sort_order) VALUES
('Vue 官网', 'https://vuejs.org', 'Vue 官方文档', 10),
('Spring 官网', 'https://spring.io', 'Spring 官方', 20),
('MDN', 'https://developer.mozilla.org', 'Web 技术文档', 30);

-- 首页轮播/背景图配置
CREATE TABLE IF NOT EXISTS home_banners (
  id          BIGINT PRIMARY KEY AUTO_INCREMENT,
  image_url   VARCHAR(500) NOT NULL COMMENT '背景图地址',
  title       VARCHAR(200) COMMENT '主标题',
  subtitle    VARCHAR(300) COMMENT '副标题',
  sort_order  INT NOT NULL DEFAULT 0 COMMENT '排序，数字越小越靠前',
  created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO home_banners (image_url, title, subtitle, sort_order) VALUES
('https://images.unsplash.com/photo-1499750310107-5fef28a66643?w=1200&h=400&fit=crop', '欢迎来到我的博客', '记录 · 分享 · 交流', 10);
