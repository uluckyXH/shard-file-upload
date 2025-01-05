# 文件上传管理系统（后端）

一个基于 Spring Boot 3 + MyBatis-Plus + SQLite 开发的现代化文件上传管理系统，支持大文件分片上传。

## 前端项目

前端项目地址：[shard-file-upload-vue](https://github.com/uluckyXH/shard-file-upload-vue)

## 待办事项

- [ ] 阿里云OSS存储适配
  - 目前仅支持本地存储
  - OSS存储策略实现待开发
  - 包含分片上传到OSS的支持

## 技术栈

- **开发框架**: Spring Boot 3.4.1
- **开发语言**: Java 17
- **ORM 框架**: MyBatis-Plus 3.5.9
- **数据库**: SQLite 3.47.2
- **工具库**: Hutool 5.8.25
- **构建工具**: Maven
- **接口文档**: Swagger/OpenAPI
- **代码规范**: Lombok

## 主要功能

### 1. 文件上传

- ✨ 支持普通上传和分片上传
- 📦 大文件分片处理
- 📊 分片上传进度跟踪
- 🔒 文件MD5完整性校验

### 2. 存储策略

- 💾 本地文件存储
- 📂 按日期自动分类存储
- 🔌 存储策略可扩展（预留OSS等接口）
- 🗃️ 文件元数据管理

### 3. 文件管理

- 📋 文件信息管理
- 🔍 文件检索功能
- 📄 文件详情查看
- 🖼️ 文件在线预览
- ⬇️ 文件下载支持

## 数据库说明

项目使用SQLite作为数据库，具有以下特点：
- 零配置，无需安装数据库服务
- 单文件存储，方便部署和迁移
- 支持完整的SQL功能
- 适合中小型应用

### SQLite配置
```yaml
sqlite:
  db:
    path: db/data.db  # 数据库文件路径

spring:
  datasource:
    driver-class-name: org.sqlite.JDBC
    url: jdbc:sqlite:db/data.db
```

### 数据库表设计

#### 文件信息表 (file_info)
```sql
CREATE TABLE file_info (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    file_name VARCHAR(255) NOT NULL,      # 文件名
    original_file_name VARCHAR(255) NOT NULL, # 原始文件名
    file_ext VARCHAR(50),                 # 文件扩展名
    storage_type VARCHAR(50) NOT NULL,     # 存储类型
    access_url TEXT,                      # 访问URL
    md5 VARCHAR(32),                      # MD5值
    upload_id VARCHAR(64),                # 上传ID
    status VARCHAR(20) NOT NULL,          # 状态
    bucket_name VARCHAR(100),             # 存储空间
    file_size BIGINT,                     # 文件大小
    create_time TIMESTAMP,                # 创建时间
    update_time TIMESTAMP                 # 更新时间
);
```

#### 分片信息表 (chunk_info)
```sql
CREATE TABLE chunk_info (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    upload_id VARCHAR(64) NOT NULL,       # 上传ID
    bucket_name VARCHAR(100),             # 存储空间
    chunk_number INTEGER NOT NULL,        # 分片序号
    upload_status VARCHAR(20) NOT NULL,   # 上传状态
    file_name VARCHAR(255) NOT NULL,      # 文件名
    original_file_name VARCHAR(255) NOT NULL, # 原始文件名
    md5 VARCHAR(32),                      # MD5值
    storage_type VARCHAR(50) NOT NULL,    # 存储类型
    file_size BIGINT,                     # 分片大小
    chunk_path TEXT,                      # 分片路径
    create_time TIMESTAMP,                # 创建时间
    update_time TIMESTAMP                 # 更新时间
);
```

## 目录结构

```
src/main/java/com/uluckyxh/shardfileupload/
├── config/          # 配置类
│   ├── data/       # 数据库配置
│   └── FileUploadConfig.java  # 文件上传配置
├── constant/        # 常量定义
├── controller/      # 控制器
├── entity/         # 实体类
├── enums/          # 枚举类
├── manage/         # 文件管理策略
│   └── impl/       # 策略实现
├── mapper/         # MyBatis接口
├── service/        # 服务接口
│   └── impl/       # 服务实现
└── util/           # 工具类
```

## 文件上传配置

```yaml
file:
  upload:
    # 存储类型：LOCAL-本地存储
    storageType: LOCAL
    # 上传目录
    uploadDir: upload-files
    # 临时文件目录
    tempDir: upload-files/temp
    # 分片大小（MB）
    chunkSize: 5
    # 最大文件大小（MB）
    maxFileSize: 3072  # 3GB
```

## 开发环境设置

1. 配置Java 17环境

2. 克隆项目
```bash
git clone https://github.com/uluckyXH/shard-file-upload.git
```

3. 使用IDE导入项目（推荐IntelliJ IDEA）

## 许可证

[Apache License 2.0](LICENSE)
