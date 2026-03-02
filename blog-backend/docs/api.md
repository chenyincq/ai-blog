# 博客后端接口文档

## 概述

| 服务       | 地址                    | 说明 |
|------------|-------------------------|------|
| 认证服务   | `http://localhost:8082` | 登录、JWT、微信扫码登录 |
| 文章服务   | `http://localhost:8081` | 文章 CRUD、标签/分类、封面上传 |

- **跨域（CORS）**：两个服务均允许 `http://localhost:5173`、`5174`、`5175` 及 `http://127.0.0.1:5173`、`5174`、`5175`；并通过全局 `CorsFilter` 允许 `http://localhost:*`、`http://127.0.0.1:*`，前端换端口也可访问。
- **管理端接口**：需在请求头中携带 `Authorization: Bearer <token>`（登录或微信回调获得的 JWT）。

---

## 一、认证服务 (blog-auth-service, 8082)

### 1. 账号密码登录

| 项目   | 说明 |
|--------|------|
| **URL** | `POST /api/auth/login` |
| **Content-Type** | `application/json` |

**Request Body**

| 字段     | 类型   | 必填 | 说明 |
|----------|--------|------|------|
| username | string | 是   | 用户名 |
| password | string | 是   | 密码   |

**示例**

```json
{ "username": "admin", "password": "admin123" }
```

**Response（200）**

```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "role": "ADMIN",
  "nickname": null,
  "avatar": null
}
```

- **401**：用户名或密码错误。

---

### 2. 获取微信授权登录 URL

| 项目   | 说明 |
|--------|------|
| **URL** | `GET /api/auth/wechat/authorize` |

**Query 参数**

| 参数  | 类型   | 必填 | 说明 |
|-------|--------|------|------|
| state | string | 否   | 防 CSRF，不传则服务端生成 |

**Response（200）**

```json
{ "url": "https://open.weixin.qq.com/connect/qrconnect?appid=..." }
```

前端将用户跳转至 `url`，用户扫码授权后，微信会重定向到认证服务配置的**回调地址**。

---

### 3. 微信登录回调（由微信服务器调用）

| 项目   | 说明 |
|--------|------|
| **URL** | `GET /api/auth/wechat/callback` |

**Query 参数**

| 参数  | 类型   | 必填 | 说明 |
|-------|--------|------|------|
| code  | string | 是   | 微信返回的授权码 |
| state | string | 否   | 与授权时一致 |

**行为**：使用 `code` 换取 access_token 和用户信息，创建或更新本系统用户，签发 JWT，并 **302 重定向** 到前端：

```
{blog.frontend-url}/#/admin/callback?token=<jwt>&nickname=<urlEncoded>
```

前端在 `/#/admin/callback` 页从 URL 读取 `token`，写入 localStorage 后跳转至管理页。

---

## 二、文章服务 (blog-post-service, 8081)

### 1. 分页查询已发布文章（支持按作者/分类/标签筛选）

| 项目   | 说明 |
|--------|------|
| **URL** | `GET /api/posts` |

**Query 参数**（参数名需与下表一致，避免“Name for argument of type [int] not specified”等绑定错误）

| 参数     | 类型   | 必填 | 默认值 | 说明         |
|----------|--------|------|--------|--------------|
| page     | int    | 否   | 0      | 页码（从 0 开始） |
| size     | int    | 否   | 10     | 每页条数     |
| author   | string | 否   | -      | 作者关键词（模糊） |
| category | string | 否   | -      | 分类关键词（模糊） |
| tag      | string | 否   | -      | 标签关键词（模糊） |

**示例请求**

```
GET http://localhost:8081/api/posts?page=0&size=8
GET http://localhost:8081/api/posts?page=0&size=10&author=管理员
GET http://localhost:8081/api/posts?page=0&size=10&tag=Vue
```

**Response（200）**

```json
{
  "content": [ { "id": 1, "title": "...", "summary": "...", ... } ],
  "totalElements": 100,
  "page": 0,
  "size": 10
}
```

---

### 2. 文章详情（仅已发布）

| 项目   | 说明 |
|--------|------|
| **URL** | `GET /api/posts/{id}` |

**路径参数**：`id` 为文章主键。

**Response（200）**：单条 `Post` 对象（参见文末数据模型）。

**404**：文章不存在或未发布。

---

### 3. 获取文章评论列表

| 项目   | 说明 |
|--------|------|
| **URL** | `GET /api/posts/{postId}/comments` |

**路径参数**：`postId` 为文章主键。

**Response（200）**：评论数组，按时间正序。

```json
[
  {
    "id": 1,
    "postId": 1,
    "author": "读者",
    "content": "评论内容",
    "createdAt": "2025-01-01T12:00:00"
  }
]
```

---

### 4. 发表评论（无需登录）

| 项目   | 说明 |
|--------|------|
| **URL** | `POST /api/posts/{postId}/comments` |
| **Content-Type** | `application/json` |

**路径参数**：`postId` 为文章主键。

**Request Body**

| 字段    | 类型   | 必填 | 说明     |
|---------|--------|------|----------|
| author  | string | 是   | 昵称     |
| content | string | 是   | 评论内容 |

**Response（200）**：创建后的评论对象。**404**：文章不存在。

---

### 5. 标签云

| 项目   | 说明 |
|--------|------|
| **URL** | `GET /api/posts/tags` |

**Response（200）**

```json
[
  { "name": "Vue", "count": 5 },
  { "name": "前端", "count": 3 }
]
```

按 `count` 降序。

---

### 6. 分类归档

| 项目   | 说明 |
|--------|------|
| **URL** | `GET /api/posts/categories` |

**Response（200）**

```json
[
  { "name": "技术", "count": 10 },
  { "name": "随笔", "count": 4 }
]
```

按 `count` 降序。

---

### 7. 管理端 - 分页查询全部文章（需 JWT）

| 项目   | 说明 |
|--------|------|
| **URL** | `GET /api/posts/admin/all` |
| **Header** | `Authorization: Bearer <token>` |

**Query 参数**

| 参数  | 类型 | 必填 | 默认值 | 说明     |
|-------|------|------|--------|----------|
| page  | int  | 否   | 0      | 页码     |
| size  | int  | 否   | 10     | 每页条数 |

**示例请求**

```
GET http://localhost:8081/api/posts/admin/all?page=0&size=10
Header: Authorization: Bearer <your-jwt>
```

**Response（200）**：与“分页查询已发布文章”相同结构，但 `content` 中包含未发布文章。

**401**：未提供或无效 token。

---

### 8. 管理端 - 新建文章（需 JWT）

| 项目   | 说明 |
|--------|------|
| **URL** | `POST /api/posts/admin` |
| **Header** | `Authorization: Bearer <token>`、`Content-Type: application/json` |

**Request Body**

| 字段      | 类型    | 必填 | 说明             |
|-----------|---------|------|------------------|
| title     | string  | 是   | 标题             |
| summary   | string  | 否   | 摘要             |
| content   | string  | 是   | 正文（Markdown） |
| author    | string  | 是   | 作者             |
| category  | string  | 否   | 分类             |
| tags      | string  | 否   | 英文逗号分隔     |
| coverUrl  | string  | 否   | 封面图 URL       |
| published | boolean | 否   | 是否发布，默认 true |

**Response（200）**：创建后的 `Post` 对象。

---

### 9. 管理端 - 更新文章（需 JWT）

| 项目   | 说明 |
|--------|------|
| **URL** | `PUT /api/posts/admin/{id}` |
| **Header** | 同“新建文章” |
| **Body**   | 同“新建文章”字段 |

**Response（200）**：更新后的 `Post`。**404**：id 不存在。

---

### 10. 管理端 - 删除文章（需 JWT）

| 项目   | 说明 |
|--------|------|
| **URL** | `DELETE /api/posts/admin/{id}` |
| **Header** | `Authorization: Bearer <token>` |

**Response**：204 No Content。**404**：id 不存在。

---

### 11. 管理端 - 上传封面图（需 JWT）

| 项目   | 说明 |
|--------|------|
| **URL** | `POST /api/posts/admin/cover` |
| **Header** | `Authorization: Bearer <token>` |
| **Body**   | `multipart/form-data`，字段名 `file`，值为图片文件 |

**Response（200）**

```json
{ "url": "http://localhost:8081/uploads/cover_1234567890.jpg" }
```

可将返回的 `url` 填入文章的 `coverUrl` 字段。图片通过文章服务的 `/uploads/**` 静态路径访问。

---

## 四、友情链接 (friend-links, 8081)

### 1. 获取友情链接列表（公开）

| 项目   | 说明 |
|--------|------|
| **URL** | `GET /api/friend-links` |

**Response（200）**：按 `sort_order` 升序的链接数组。

```json
[
  {
    "id": 1,
    "name": "Vue 官网",
    "url": "https://vuejs.org",
    "description": "Vue 官方文档",
    "sortOrder": 10,
    "createdAt": "2025-01-01T12:00:00"
  }
]
```

---

### 2. 管理端 - 获取全部友情链接（需 JWT）

| 项目   | 说明 |
|--------|------|
| **URL** | `GET /api/friend-links/admin/all` |
| **Header** | `Authorization: Bearer <token>` |

**Response（200）**：同上，链接数组。

---

### 3. 管理端 - 新建友情链接（需 JWT）

| 项目   | 说明 |
|--------|------|
| **URL** | `POST /api/friend-links/admin` |
| **Header** | `Authorization: Bearer <token>`、`Content-Type: application/json` |

**Request Body**

| 字段        | 类型   | 必填 | 说明                     |
|-------------|--------|------|--------------------------|
| name        | string | 是   | 链接名称                 |
| url         | string | 是   | 链接地址                 |
| description | string | 否   | 简要描述                 |
| sortOrder   | int    | 否   | 排序，数字越小越靠前，默认 0 |

**Response（200）**：创建后的链接对象。

---

### 4. 管理端 - 更新友情链接（需 JWT）

| 项目   | 说明 |
|--------|------|
| **URL** | `PUT /api/friend-links/admin/{id}` |
| **Header** | 同“新建友情链接” |
| **Body**   | 同“新建友情链接”字段 |

**Response（200）**：更新后的链接。**404**：id 不存在。

---

### 5. 管理端 - 删除友情链接（需 JWT）

| 项目   | 说明 |
|--------|------|
| **URL** | `DELETE /api/friend-links/admin/{id}` |
| **Header** | `Authorization: Bearer <token>` |

**Response**：204 No Content。**404**：id 不存在。

---

## 五、数据模型 Post

```json
{
  "id": 1,
  "title": "标题",
  "summary": "摘要",
  "content": "正文 Markdown",
  "author": "作者",
  "category": "分类",
  "tags": "标签1,标签2",
  "coverUrl": "http://localhost:8081/uploads/cover_xxx.jpg",
  "createdAt": "2025-01-01T12:00:00",
  "updatedAt": "2025-01-01T12:00:00",
  "published": true
}
```

---

## 六、微信登录配置说明

1. 在 [微信开放平台](https://open.weixin.qq.com/) 创建「网站应用」，获取 **AppID**、**AppSecret**。
2. 在应用内配置 **授权回调域**（如 `localhost` 或你的域名）。完整回调地址为：  
   `http://<认证服务域名:端口>/api/auth/wechat/callback`  
   本地示例：`http://localhost:8082/api/auth/wechat/callback`（需可被微信公网访问，内网需做穿透）。
3. 在认证服务 `application.yml` 或环境变量中配置：
   - `wechat.app-id`、`wechat.app-secret`
   - `wechat.redirect-uri`（与上述回调地址一致）
   - `blog.frontend-url`（前端地址，登录成功后跳转，如 `http://localhost:5173`）

---

## 七、常见错误与说明

| 现象 | 说明 |
|------|------|
| 跨域（CORS）报错 | 已配置多端口及 `CorsFilter`，支持 `localhost:*`、`127.0.0.1:*`；若仍报错请确认后端已重启。 |
| `Name for argument of type [int] not specified` | 文章服务已对 `page`、`size` 等使用 `@RequestParam(name = "page", ...)` 显式指定参数名，请确保使用最新代码并重启服务。 |
| 401 Unauthorized | 管理端接口需在 Header 中携带有效 JWT；token 过期或未登录会返回 401。 |
