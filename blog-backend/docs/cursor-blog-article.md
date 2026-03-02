# 如何使用 Cursor 构建一个博客网站

本文介绍如何借助 Cursor 编辑器，从零搭建一个前后端分离的博客网站，包括具体步骤与可复用的实践建议。

## 一、Cursor 是什么，为什么用它建博客

**Cursor** 是一款基于 VS Code 的 AI 编程编辑器，内置大模型能力，可以在写代码时进行补全、解释、重构和按自然语言生成/修改代码。用它来构建博客的好处包括：

- **快速搭架子**：用自然语言描述「做一个 Vue 3 博客首页」，即可生成页面骨架与路由。
- **减少查文档**：不熟悉的库（如 Vue Router、Spring Boot）可直接在 Cursor 里问「如何做 xxx」并插入代码。
- **统一环境**：写前端、改后端、调 SQL 都在同一个项目里完成，AI 能结合整个仓库上下文给出建议。

适合人群：有一定前端或后端基础，希望提高搭建效率的开发者。

## 二、环境准备

在开始前请确保本机已安装：

| 工具 | 用途 | 建议版本 |
|------|------|----------|
| **Node.js** | 运行前端 (Vue + Vite) | 18+ |
| **Java** | 运行后端 (Spring Boot) | 17+ |
| **Maven** | 构建后端项目 | 3.8+ |
| **MySQL** | 存储文章、评论、用户 | 8.0+ |
| **Cursor** | 编辑器 + AI 辅助 | 最新版 |

安装 Cursor：打开 [cursor.com](https://cursor.com) 下载对应系统版本，安装后用已有项目目录打开即可。

## 三、项目结构概览

推荐采用**前后端分离 + 微服务**：

- **前端**：Vue 3 + Vite，负责页面、路由、调用后端 API。
- **后端**：Spring Boot，拆成多个服务，例如：
  - **博客文章服务**：文章的 CRUD、分页、标签/分类；
  - **认证服务**：登录、JWT 签发与校验；
  - **评论 / 友情链接 / 轮播图**：可按需单独服务或合在一个服务里。

这样 Cursor 可以根据「打开的文件」和「当前服务」给出更精准的修改建议。

## 四、后端搭建具体步骤

### 1. 创建 Spring Boot 项目

在 Cursor 里可以直接对 AI 说：

> 「用 Spring Initializr 创建一个 Spring Boot 项目，包含 Spring Web、Spring Data JPA、MySQL Driver，模块名 blog-post-service。」

或手动在 [start.spring.io](https://start.spring.io/) 勾选上述依赖，生成后解压到仓库的 `blog-backend/blog-post-service` 目录。

### 2. 配置数据库与 JPA

在 `application.yml` 中配置数据源和 JPA（库名、用户名、密码按本机修改）：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/blog_db?useUnicode=true&characterEncoding=utf8mb4
    username: root
    password: your_password
  jpa:
    hibernate:
      ddl-auto: update
    show-sql: false
```

执行项目中的 `docs/scripts/db-init.sql`（或你维护的建表脚本），创建 `posts`、`comments`、`users` 等表。

### 3. 编写实体、Repository 与 Controller

- **实体**：对应表结构，如 `Post`（id、title、content、author、createdAt、published 等）。
- **Repository**：继承 `JpaRepository<Post, Long>`，即可获得分页、按 ID 查询等方法。
- **Controller**：  
  - 对外的 `GET /api/posts`（分页）、`GET /api/posts/{id}`；  
  - 管理端 `POST /api/posts/admin`（需 JWT）、`PUT /api/posts/admin/{id}`、`DELETE /api/posts/admin/{id}`。

在 Cursor 中你可以说：「根据 Post 实体生成一个 PostController，提供分页列表和根据 id 查询详情」，让 AI 生成方法骨架，再按需调整路径和返回格式。

### 4. 认证与鉴权

- 登录接口：校验用户名密码（或接微信登录），成功后签发 JWT。
- 管理端接口：在过滤器或拦截器中校验 `Authorization: Bearer <token>`，无效则返回 401。

把认证单独做成一个服务（如 auth-service），博客服务通过校验 JWT 即可，便于在 Cursor 里分模块修改。

## 五、前端搭建具体步骤

### 1. 创建 Vue 3 项目

在仓库中新建前端目录，在终端执行：

```bash
npm create vite@latest blog-frontend -- --template vue
cd blog-frontend && npm install
```

安装路由、请求库（或用原生 fetch）：`npm install vue-router`。在 Cursor 中打开 `blog-frontend`，后续所有前端修改都在这里进行。

### 2. 配置路由与 API 基地址

- 在 `src/router/index.js` 中配置路由：`/` 首页、`/posts` 文章列表、`/post/:id` 文章详情、`/admin/login` 管理登录、`/admin/posts` 文章管理等。
- 在 `src/api.js`（或类似文件）中封装 `getPosts`、`getPost(id)`、`createPost`、`login` 等，请求地址指向后端服务（如 `http://localhost:8081/api/posts`）。开发时可配合 Vite 的 proxy 解决跨域。

### 3. 主要页面与组件

- **首页**：轮播图 + 最新文章列表卡片，数据来自 `getPosts()`、`getHomeBanners()`。
- **文章列表页**：分页、筛选（标签/分类），调用带分页参数的 `getPosts()`。
- **文章详情页**：根据路由参数 id 调用 `getPost(id)`，渲染标题、正文（Markdown 转 HTML）、评论列表。
- **管理后台**：登录后保存 token，列表页调用 `getAdminPosts(token)`，新建/编辑时调用 `createPost`/`updatePost`，删除调用 `deletePost`。

在 Cursor 里可以对某个 Vue 文件说：「在这个页面加上分页按钮」或「把摘要改成最多两行」，让 AI 直接改对应模板与逻辑。

### 4. 主题与样式

用 CSS 变量统一管理颜色（如 `--bg`、`--text`、`--accent`），在根节点切换 class（如 `theme-light` / `theme-dark`）实现日夜间主题；把用户选择存到 `localStorage`，下次进入时应用。这样 Cursor 在改样式时也更容易保持风格一致。

## 六、使用 Cursor 的具体操作建议

1. **从需求到代码**：在聊天里写清需求，例如「在首页增加一个天气组件，显示当前城市和温度」，并指定文件或组件，让 AI 生成或修改代码后再自己跑一遍确认。
2. **利用项目上下文**：用 @ 引用具体文件或文件夹，让 AI 基于现有 API、路由和样式做修改，避免接口或命名不一致。
3. **分步实现**：先让「文章能列表、能点进详情」，再做评论、管理后台、登录，每一步都在 Cursor 里描述清楚，便于生成可运行的小步提交。
4. **调试与报错**：把终端报错或浏览器控制台错误贴给 Cursor，让它帮你改依赖、改语法或改接口字段。
5. **数据库与 SQL**：建表、插数据、简单查询可以直接在 Cursor 里写 SQL 或让 AI 根据实体生成，再在 MySQL 客户端或后端初始化脚本中执行。

## 七、运行与部署

- **本地运行**：  
  - 启动 MySQL，执行建表与初始数据脚本。  
  - 启动后端：`mvn spring-boot:run`（或在 IDE 中运行主类）。  
  - 启动前端：`npm run dev`，浏览器打开 Vite 给出的地址（如 http://localhost:5173）。  
- **部署**：前端 `npm run build` 后把产物放到 Nginx 或对象存储；后端打成 jar 用 `java -jar` 或 Docker 运行，并配置好数据库地址和 JWT 密钥等环境变量。

## 八、小结

用 Cursor 构建博客的核心是：**把「要做什么」说清楚**，并**结合现有项目结构**让 AI 生成或修改代码。从环境准备、后端接口、前端页面到主题与部署，都可以在 Cursor 里一步步完成；遇到不熟悉的框架或报错，直接提问即可。按本文步骤搭好架子后，你可以继续用 Cursor 迭代功能、写新文章或优化样式，逐步完善自己的博客站。
