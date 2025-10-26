# 🎵 音乐猜歌游戏

一个基于Spring Boot和原生HTML/CSS/JavaScript的音乐猜歌游戏。

## ✨ 功能特性

- 🎧 **猜歌挑战** - 随机播放歌曲，测试音乐知识
- 📤 **歌曲上传** - 支持多种音频格式，最大5MB
- 🎵 **歌曲管理** - 查看、播放、删除已上传的歌曲
- 🎯 **智能随机** - 会话级去重，避免重复播放
- 📱 **响应式设计** - 支持桌面和移动设备

## 🚀 快速开始

### 环境要求
- Java 17+
- Maven 3.6+
- MySQL 8.0+

### 数据库配置
1. 创建数据库：
```sql
CREATE DATABASE vocaloid_game CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

2. 修改数据库连接配置（`backend/src/main/resources/application.yml`）：
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/vocaloid_game?serverTimezone=Asia/Shanghai&useSSL=false&allowPublicKeyRetrieval=true
    username: your_username
    password: your_password
```

### 启动应用
```bash
# 进入后端目录
cd backend

# 构建应用
mvn clean package -DskipTests

# 启动应用
java -jar target/vocaloid-game-1.0.0.jar
```

### 访问应用
- 应用地址：http://localhost:10001
- 支持所有现代浏览器

## 📁 项目结构

```
vocaloid-game/
├── backend/                    # Spring Boot后端
│   ├── src/main/java/          # Java源码
│   │   └── com/vocaloid/
│   │       ├── config/         # 配置类
│   │       ├── controller/     # 控制器
│   │       ├── entity/         # 实体类
│   │       ├── repository/     # 数据访问层
│   │       └── service/        # 业务逻辑层
│   ├── src/main/resources/
│   │   ├── application.yml     # 应用配置
│   │   └── static/            # 静态资源
│   │       ├── index.html    # 主页面
│   │       ├── styles.css     # 样式文件
│   │       └── app.js         # JavaScript功能
│   └── pom.xml               # Maven配置
└── README.md                 # 项目说明
```

## 🎯 技术栈

### 后端
- **Spring Boot 3.2** - 主框架
- **Spring Data JPA** - 数据访问
- **MySQL 8.0** - 数据库
- **Maven** - 构建工具

### 前端
- **HTML5** - 页面结构
- **CSS3** - 样式设计
- **原生JavaScript** - 交互逻辑
- **Fetch API** - 异步请求

## 🔧 核心功能

### 智能随机算法
- 会话级去重，避免重复播放
- 自动重置机制
- 多用户支持

### 文件上传
- 支持MP3、WAV、M4A等格式
- 5MB文件大小限制
- BASE64编码存储

### 音频播放
- HTML5 Audio API
- 播放控制
- 进度显示

## 📱 界面特色

- 现代化渐变背景
- 毛玻璃效果卡片
- 响应式布局
- 平滑动画效果
- 移动端适配

## 🛠️ 开发说明

### 添加新功能
1. 后端：在相应的包中添加Java类
2. 前端：修改`static/`目录下的HTML/CSS/JS文件
3. 重新构建：`mvn clean package -DskipTests`

### 自定义样式
- 修改`static/styles.css`
- 支持CSS3新特性
- 响应式设计

### API接口
- `/api/music/random` - 获取随机歌曲
- `/api/music/upload` - 上传歌曲
- `/api/music/list` - 获取歌曲列表
- `/api/music/play/{id}` - 播放歌曲
- `/api/music/{id}` - 删除歌曲

## 📄 许可证

MIT License

## 🤝 贡献

欢迎提交Issue和Pull Request！

---

🎵 享受音乐猜歌的乐趣！