# AI Blog

前后端分离的个人博客系统：Vue 3 + Spring Boot 微服务 + MySQL。

> **本项目由 [Cursor](https://cursor.com) 辅助生成**：通过自然语言描述需求，由 AI 生成前后端代码、数据库脚本与配置文件，开发效率显著提升。下方「由 Cursor 如何生成」说明了具体流程。

## 由 Cursor 如何生成

本项目的主要文件与功能均通过 **Cursor 编辑器** 的 AI 能力完成，典型流程如下：

1. **描述需求**：在 Cursor 聊天中直接说明要做什么，例如「做一个 Vue 3 博客首页，包含轮播图和文章列表」或「后端增加文章浏览量、热门文章接口」。
2. **生成代码**：AI 根据描述生成或修改对应文件（Vue 组件、Spring Boot Controller、JPA 实体、SQL 脚本等），可指定目录或文件，让 AI 基于现有结构补充。
3. **引用上下文**：使用 `@` 引用项目内文件或文件夹，让 AI 结合已有 API、路由、样式做修改，避免接口与命名不一致。
4. **迭代完善**：对生成结果提出修改，如「标题单行省略」「简介悬浮显示全文」「删除点赞功能」，AI 会按描述调整代码。
5. **调试与文档**：遇到报错或需补充文档时，将终端输出、错误信息或需求贴给 Cursor，由 AI 修改代码或生成说明文档。

在上述流程中，前后端结构、数据库表、接口设计、样式和 README 等内容均由 Cursor 辅助生成或优化，博客内亦有《如何使用 Cursor 构建一个博客网站》一文可供参考。

## 技术栈

| 层级   | 技术 |
|--------|------|
| 前端   | Vue 3、Vue Router、Vite、Marked |
| 后端   | Spring Boot 3、Spring Data JPA、MySQL、JWT、微信开放平台 OAuth2 |
| 数据库 | MySQL 8.x |

## 项目结构

```
ai-blog/
├── blog-backend/           # 后端
│   ├── db-init.sql         # 数据库初始化
│   ├── blog-gateway/       # API 网关（端口 8080，统一转发）
│   ├── blog-auth-service/  # 认证服务（端口 8082）
│   └── blog-post-service/  # 文章服务（端口 8081）
└── blog-frontend/          # 前端（端口 5173）
```

## 快速开始

### 1. 数据库

```bash
mysql -h <host> -u root -p < blog-backend/db-init.sql
```

默认管理员：`admin` / `admin123`。数据库地址需在 `blog-backend/*/src/main/resources/application.yml` 中配置。

### 2. 后端

需 **JDK 17**、**Maven**。前端统一请求网关 8080，网关转发到对应微服务。

```bash
# 1. 认证服务（端口 8082）
cd blog-backend/blog-auth-service && mvn spring-boot:run

# 2. 文章服务（端口 8081，另开终端）
cd blog-backend/blog-post-service && mvn spring-boot:run

# 3. 网关（端口 8080，另开终端）
cd blog-backend/blog-gateway && mvn spring-boot:run
```

### 3. 前端

需 **Node.js 18+**。

```bash
cd blog-frontend && npm install && npm run dev
```

浏览器访问：http://localhost:5173

## 功能概览

- **前台**：首页、文章列表/详情、标签与分类筛选、评论、日/夜主题、阅读量、热门文章
- **后台**：文章 CRUD、封面上传、账号密码或微信扫码登录

## 文档

- 接口说明：`blog-backend/docs/api.md`
- 微信登录配置见上述文档中的「微信登录配置说明」

## License

可按需修改与使用。
