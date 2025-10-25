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
- **说明**: 歌曲管理页面

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
- **响应**: 成功返回 "歌曲上传成功！"，失败返回错误信息

### 4. 获取所有歌曲列表
- **URL**: `/api/music/list`
- **方法**: GET
- **说明**: 获取所有歌曲，按上传时间倒序排列
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

### 5. 播放歌曲
- **URL**: `/api/music/play/{id}`
- **方法**: GET
- **参数**: `id` - 歌曲ID
- **说明**: 获取歌曲的音频文件
- **响应**: 音频文件流，Content-Type: `audio/mpeg`

### 6. 删除歌曲
- **URL**: `/api/music/{id}`
- **方法**: DELETE
- **参数**: `id` - 歌曲ID
- **响应**: 成功返回 "歌曲删除成功"，失败返回错误信息

### 7. 重置会话
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
3. 支持的音频格式：MP3等常见音频格式
4. 会话ID基于IP和User-Agent自动生成
5. 智能随机算法会避免在单次会话中重复播放同一首歌曲
