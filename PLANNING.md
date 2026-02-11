# SayHi 后端 - Todo List

> 最后更新：2026-02-10

## 技术栈
- Spring Boot 3 + WebSocket + Spring Security + JPA
- 数据库：PostgreSQL + Redis

---

## Todo

### Phase 1: 用户系统（注册/登录/JWT） ✅ DONE
- [x] 添加 Spring Security, JPA, PostgreSQL, JWT 依赖
- [x] 创建 users 数据表
- [x] 实现用户注册 API
- [x] 实现用户登录 API
- [x] 实现 JWT 认证过滤器

---

### Phase 2: 好友系统 ✅ DONE
- [x] 创建 friendships 数据表
- [x] 创建 Friendship 实体
- [x] 创建 FriendshipRepository
- [x] 实现好友请求 API (POST /api/friends/request)
- [x] 实现接受/拒绝好友请求 API
- [x] 实现好友列表 API (GET /api/friends)
- [x] 实现删除好友 API

---

### Phase 3: 单聊消息（WebSocket） ✅ DONE
- [x] 添加 WebSocket, Redis 依赖
- [x] 创建 messages 数据表
- [x] 创建 Message 实体
- [x] 实现 WebSocket STOMP 端点 (/ws/chat)
- [x] 实现 Redis 在线状态管理
- [x] 实现消息缓存逻辑
- [x] 实现历史消息 API

---

### Phase 4: 群聊
- [x] 创建 groups 数据表
- [x] 创建 group_members 数据表
- [x] 修改 messages 表添加 group_id
- [x] 实现创建群组 API
- [x] 实现邀请成员 API
- [x] 实现群消息广播

---

### Phase 5: 文件服务
- [x] 实现文件上传 API (本地存储)
- [x] 支持 JPEG, PNG, GIF, WebP 格式
- [x] 限制文件大小 10MB
- [x] 配置静态资源访问 /uploads/**

---

## API 端点

| 方法 | 路径 | 说明 |
|------|------|------|
| POST | `/api/auth/register` | 用户注册 |
| POST | `/api/auth/login` | 用户登录 |
| GET | `/api/users/me` | 获取当前用户 |
| PUT | `/api/users/me` | 更新用户信息 |
| GET | `/api/users/search` | 搜索用户 |
| GET | `/api/users/{id}` | 获取用户信息 |
| GET | `/api/friends` | 获取好友列表 |
| POST | `/api/friends/request` | 发送好友请求 |
| PUT | `/api/friends/request/{id}` | 接受/拒绝请求 |
| DELETE | `/api/friends/{id}` | 删除好友 |
| GET | `/api/messages` | 获取聊天列表 |
| GET | `/api/messages/history/{userId}` | 获取聊天记录 |
| POST | `/api/messages` | 发送消息 |
| PUT | `/api/messages/read/{userId}` | 标记已读 |

## WebSocket
- 端点：`/ws/chat`
- 订阅：`/topic/messages/{userId}`

---

## 服务端口
| 服务 | 端口 |
|------|------|
| backend-kotlin | 8080 |
| PostgreSQL | 5432 |
| Redis | 6379 |
