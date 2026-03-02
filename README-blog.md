# 博客项目说明

## 项目简介

本项目是一个**前后端分离**的博客系统：前端使用 **Vue 3**，后端采用 **Spring Boot 微服务**，数据库为 **MySQL**。支持文章展示、按作者/标签/分类筛选、标签云与分类归档、封面图上传、管理后台（文章 CRUD），以及 **微信开放平台网站应用扫码登录**。

---

## 技术栈

| 层级     | 技术 |
|----------|------|
| 前端     | Vue 3、Vue Router、Vite、Marked（Markdown 渲染） |
| 后端     | Spring Boot 3、Spring Data JPA、MySQL、JWT（jjwt）、微信开放平台 OAuth2 |
| 数据库   | MySQL 8.x（host: 192.168.100.183，用户名 root，密码 smark@park） |

---

## 项目结构

```
ai-blog/
├── blog-backend/                     # 后端
│   ├── db-init.sql                   # MySQL 初始化（库、表、默认管理员、测试文章）
│   ├── docs/
│   │   └── api.md                    # 后端接口文档（必读）
│   ├── blog-auth-service/            # 认证微服务（端口 8082）
│   │   └── src/
│   │       ├── config/CorsConfig.java   # CORS 全局配置
│   │       ├── auth/...                 # 登录、JWT、微信登录
│   └── blog-post-service/            # 文章微服务（端口 8081）
│       └── src/
│           ├── config/CorsConfig.java   # CORS 全局配置
│           ├── config/WebConfig.java    # 静态资源 /uploads/**
│           └── post/...                 # 文章 CRUD、标签/分类、封面上传、JWT 校验
│
└── blog-frontend/                    # 前端（默认端口 5173，占用时为 5174 等）
    ├── index.html
    ├── src/
    │   ├── main.js
    │   ├── App.vue
    │   ├── api.js                    # 接口封装（POST_API、AUTH_API）
    │   ├── router/
    │   ├── views/                    # 首页、文章列表/详情、管理登录/回调/文章管理
    │   └── style.css                 # 全局样式（深色主题、Noto Serif SC + DM Sans）
    └── package.json
```

---

## 环境与配置

### 1. 数据库

- **MySQL** 需可访问：host `192.168.100.183`，端口 3306。
- 执行初始化脚本（创建库、表、默认管理员及测试文章）：
  ```bash
  mysql -h 192.168.100.183 -u root -p'smark@park' < blog-backend/db-init.sql
  ```
- 若数据库已存在且需单独增加评论表，可执行：  
  `mysql -h 192.168.100.183 -u root -p'smark@park' < blog-backend/db-comments.sql`
- 默认管理员：用户名 `admin`，密码 `admin123`。
- 若 MySQL 地址/账号不同，请修改两个微服务的 `application.yml` 中的 `spring.datasource`。

### 2. 后端

- **JDK 17**、**Maven**。
- 启动前需已创建数据库并执行 `db-init.sql`。启动顺序任意：
  - 认证服务：`cd blog-backend/blog-auth-service && mvn spring-boot:run`（端口 8082）
  - 文章服务：`cd blog-backend/blog-post-service && mvn spring-boot:run`（端口 8081）
- 也可在 IntelliJ IDEA 中分别运行 `AuthServiceApplication`、`PostServiceApplication`。
- 文章服务将上传的封面保存在运行目录下的 `uploads/`，通过 `/uploads/**` 提供访问。

### 3. 前端

- **Node.js 18+**、npm。
- 安装并启动：
  ```bash
  cd blog-frontend && npm install && npm run dev
  ```
  默认地址：`http://localhost:5173`（若端口被占用，Vite 会使用 5174 等）。
- 生产构建：`npm run build`，产物在 `dist/`。

### 4. 微信登录（可选）

- 在 [微信开放平台](https://open.weixin.qq.com/) 创建「网站应用」，获取 AppID、AppSecret。
- 配置授权回调域，回调 URL 为：  
  `http://<认证服务地址:8082>/api/auth/wechat/callback`
- 在认证服务中配置（环境变量或 `application.yml`）：
  - `wechat.app-id`、`wechat.app-secret`
  - `wechat.redirect-uri`（与开放平台回调 URL 一致）
  - `blog.frontend-url`（前端地址，如 `http://localhost:5173`）

详细说明见 **`blog-backend/docs/api.md`** 中的「微信登录配置说明」。

---

## 功能说明

- **前台**
  - 首页：展示最新若干篇文章卡片（含摘要、标签；有封面时显示封面）。
  - 文章列表：分页，按作者/标签/分类筛选；侧栏标签云、分类归档，点击即筛选。
  - 文章详情：标题、作者、分类、标签、封面、Markdown 正文渲染；**评论列表**与**发表评论**（昵称 + 内容，无需登录）。
  - **日/夜模式**：右上角按钮切换，偏好保存在 localStorage。

- **管理后台**
  - 登录：支持账号密码（admin / admin123）或「微信登录」跳转扫码。
  - 微信回调页：`/#/admin/callback?token=...` 接收 token 并写入 localStorage，再跳转文章管理。
  - 文章管理：分页列表、新建/编辑/删除、分类/标签/封面 URL、封面上传、Markdown 实时预览、发布开关。
  - 所有管理端请求需携带 JWT（`Authorization: Bearer <token>`），由文章服务统一校验。

- **设计**
  - 深色主题，Noto Serif SC（正文/标题）+ DM Sans（UI），卡片式布局，带封面与标签样式。

---

## 接口文档

后端接口的完整说明见：**`blog-backend/docs/api.md`**，包括：

- 认证：账号密码登录、获取微信授权 URL、微信回调说明。
- 文章：分页列表（含筛选参数说明与示例 URL）、详情、**评论列表与发表评论**、标签云、分类归档。
- 管理：分页列表、新建、更新、删除、封面上传；请求头与参数说明。
- CORS 说明、数据模型 Post、常见错误（跨域、参数绑定、401）。

---

## 常见问题

1. **登录或请求接口时跨域（CORS）报错**  
   两个后端服务已配置：
   - Controller 上允许 `http://localhost:5173`、`5174`、`5175` 及 `http://127.0.0.1:5173`、`5174`、`5175`；
   - 全局 `CorsFilter` 允许 `http://localhost:*`、`http://127.0.0.1:*`。  
   若仍报错，请确认两个后端已重启；若前端使用其他域名或端口，可在对应服务的 `CorsConfig` 中增加 origin 或 pattern。

2. **访问 `GET /api/posts?page=0&size=8` 报错：Name for argument of type [int] not specified**  
   文章服务已对所有分页与筛选参数使用 `@RequestParam(name = "page", ...)` 等形式显式指定参数名。请拉取最新代码并重新编译、重启文章服务（8081）。

3. **MySQL 连接失败**  
   检查 192.168.100.183 是否可达、MySQL 是否允许该主机连接、账号密码是否正确（密码中的 `@` 在 YAML 中需加引号：`"smark@park"`）。

4. **微信登录无法使用**  
   确认开放平台应用已审核通过（或使用测试号）、回调域与回调 URL 配置正确、认证服务 `wechat.*` 与 `blog.frontend-url` 配置正确；本地开发时回调地址需能被微信公网访问（可借助内网穿透）。

5. **封面上传或管理接口返回 401**  
   管理端接口需先登录，并在请求头中携带有效 JWT。文章服务与认证服务使用相同 JWT 密钥校验；若修改密钥，需两处一致。

---

## 许可证

本项目为示例项目，可按需修改与使用。
