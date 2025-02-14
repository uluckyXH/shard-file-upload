-- 文件表
CREATE TABLE IF NOT EXISTS file_info (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    file_name VARCHAR(255) NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    file_ext VARCHAR(50),
    storage_type VARCHAR(50) NOT NULL,
    access_url TEXT,
    md5 VARCHAR(32),
    upload_id VARCHAR(64),
    status VARCHAR(20) NOT NULL,
    bucket_name VARCHAR(100),
    file_size BIGINT,
    create_time TIMESTAMP DEFAULT (datetime('now', 'localtime')),  -- 使用本地时间
    update_time TIMESTAMP DEFAULT (datetime('now', 'localtime'))
);

-- 分片临时表
CREATE TABLE IF NOT EXISTS chunk_info (
    id INTEGER PRIMARY KEY AUTOINCREMENT,
    upload_id VARCHAR(64) NOT NULL,
    bucket_name VARCHAR(100),
    chunk_number INTEGER NOT NULL,
    upload_status VARCHAR(20) NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    original_file_name VARCHAR(255) NOT NULL,
    md5 VARCHAR(32),
    storage_type VARCHAR(50) NOT NULL,
    file_size BIGINT,
    chunk_path TEXT,
    create_time TIMESTAMP DEFAULT (datetime('now', 'localtime')),  -- 使用本地时间
    update_time TIMESTAMP DEFAULT (datetime('now', 'localtime'))
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_file_upload_id ON file_info(upload_id);
CREATE INDEX IF NOT EXISTS idx_chunk_upload_id ON chunk_info(upload_id); 