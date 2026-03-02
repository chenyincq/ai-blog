# AI Blog

前后端分离的个人博客系统：Vue 3 + Spring Boot 微服务 + MySQL。

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

需 **JDK 17**、**Maven**。

```bash
# 认证服务
cd blog-backend/blog-auth-service && mvn spring-boot:run

# 文章服务（另开终端）
cd blog-backend/blog-post-service && mvn spring-boot:run
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
