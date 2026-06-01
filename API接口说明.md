# Vocaloid猜歌游戏 API 接口说明

## 页面访问

### 1. 猜歌游戏页面
- **URL**: `http://localhost:10001/` 或 `http://localhost:10001/index.html`
- **方法**: GET
- **说明**: 猜歌游戏主页面

### 2. 上传歌曲页面
- **URL**: `http://localhost:10001/upload` 或 `http://localhost:10001/upload.html`
- **方法**: GET
- **说明**: 歌曲上传页面

### 3. 管理歌曲页面
- **URL**: `http://localhost:10001/manage` 或 `http://localhost:10001/manage.html`
- **方法**: GET
- **说明**: 歌曲管理页面（需要密钥验证）
- **注意**: 访问时需要输入管理员密钥（默认：Mikumikuoeeo）

---

## API 接口

### 1. 获取随机歌曲
- **URL**: `/api/music/random`
- **方法**: GET
- **说明**: 获取一首随机歌曲（智能随机，避免重复）
- **请求头**: 无特殊要求
- **响应**: 
  ```json
  {
    "id": 1,
    "title": "歌曲名称",
    "uploadTime": "2024-01-01T12:00:00"
  }
  ```

### 2. 获取会话统计信息
- **URL**: `/api/music/stats`
- **方法**: GET
- **说明**: 获取当前会话的统计信息
- **响应**:
  ```json
  {
    "playedCount": 5,
    "remainingCount": 15
  }
  ```

### 3. 上传歌曲
- **URL**: `/api/music/upload`
- **方法**: POST
- **Content-Type**: `multipart/form-data`
- **参数**:
  - `file`: 音频文件（必填，最大5MB）
  - `title`: 歌曲名称（必填，最大255字符）
- **请求头**: 
  - `X-Admin-Secret`: 管理员密钥（必填）
- **响应**: 成功返回 "歌曲上传成功！"，失败返回错误信息

### 4. 验证密钥
- **URL**: `/api/music/check`
- **方法**: POST
- **Content-Type**: `application/json`
- **参数**:
  ```json
  {
    "secretKey": "管理员密钥"
  }
  ```
- **响应**: 成功返回 "密钥验证成功"，失败返回 "密钥错误"（401状态码）

### 5. 获取所有歌曲列表
- **URL**: `/api/music/list`
- **方法**: GET
- **说明**: 获取所有歌曲，按上传时间倒序排列
- **请求头**: 
  - `X-Admin-Secret`: 管理员密钥（必填）
- **响应**:
  ```json
  [
    {
      "id": 1,
      "title": "歌曲名称",
      "uploadTime": "2024-01-01T12:00:00"
    }
  ]
  ```

### 6. 播放歌曲
- **URL**: `/api/music/play/{id}`
- **方法**: GET
- **参数**: `id` - 歌曲ID
- **说明**: 获取歌曲的音频文件
- **响应**: 音频文件流，Content-Type: `audio/mpeg`

### 6. 删除歌曲
- **URL**: `/api/music/{id}`
- **方法**: DELETE
- **参数**: `id` - 歌曲ID
- **请求头**: 
  - `X-Admin-Secret`: 管理员密钥（必填）
- **响应**: 成功返回 "歌曲删除成功"，失败返回错误信息

### 7. 删除所有歌曲
- **URL**: `/api/music/all`
- **方法**: DELETE
- **请求头**: 
  - `X-Admin-Secret`: 管理员密钥（必填）
- **响应**: 返回删除的歌曲数量

### 8. 修改歌曲名称
- **URL**: `/api/music/{id}/title`
- **方法**: PUT
- **Content-Type**: `application/json`
- **参数**:
  ```json
  {
    "title": "新的歌曲名称"
  }
  ```
- **请求头**: 
  - `X-Admin-Secret`: 管理员密钥（必填）
- **响应**: 成功返回 "歌曲名称修改成功"，失败返回错误信息

### 9. 批量上传歌曲
- **URL**: `/api/music/batch-upload`
- **方法**: POST
- **Content-Type**: `multipart/form-data`
- **参数**:
  - `files`: 多个音频文件（必填，每个最大5MB）
- **请求头**: 
  - `X-Admin-Secret`: 管理员密钥（必填）
- **说明**: 支持批量上传文件夹中的所有歌曲，歌曲名称自动从文件名提取（去掉扩展名）
- **支持格式**: .mp3, .wav, .ogg
- **响应**: 返回上传结果统计信息，包括成功和失败的数量

### 10. 重置会话
- **URL**: `/api/music/reset`
- **方法**: POST
- **说明**: 重置当前会话，清空已播放记录
- **响应**: "会话已重置"

---

## 错误码说明

- **200**: 成功
- **400**: 请求错误（参数错误、文件过大等）
- **404**: 资源不存在
- **500**: 服务器内部错误

---

## 注意事项

1. 所有API都支持跨域请求（`@CrossOrigin`）
2. 文件上传限制为5MB
3. 支持的音频格式：MP3、WAV、OGG等常见音频格式
4. 会话ID基于IP和User-Agent自动生成
5. 智能随机算法会避免在单次会话中重复播放同一首歌曲
6. **管理接口需要密钥验证**：所有管理相关的API都需要在请求头中包含 `X-Admin-Secret` 字段
7. **默认管理员密钥**：`Mikumikuoeeo`（可在 `SecurityConfig.java` 中修改）
8. **批量上传**：支持选择整个文件夹进行批量上传，歌曲名称自动从文件名提取
9. **密钥缓存**：前端会将验证通过的密钥存储在localStorage中，下次访问时自动验证，无需重复输入
