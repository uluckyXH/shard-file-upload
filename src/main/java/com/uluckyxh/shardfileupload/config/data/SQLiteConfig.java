package com.uluckyxh.shardfileupload.config.data;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.sqlite.SQLiteDataSource;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

@Slf4j
@Configuration
public class SQLiteConfig {

    // 从配置文件中读取数据库文件路径
    @Value("${sqlite.db.path}")
    private String dbPath;

    @Bean
    public DataSource dataSource() {
        // 1. 确保数据库目录存在
        File dbDir = new File(dbPath).getParentFile();
        if (!dbDir.exists()) {
            // 创建多级目录
            dbDir.mkdirs();
        }

        // 2. 创建SQLite数据源
        SQLiteDataSource dataSource = new SQLiteDataSource();
        // 设置数据库文件路径
        dataSource.setUrl("jdbc:sqlite:" + dbPath);

        // 3. 初始化数据库表
        initDatabaseTables(dataSource);

        log.info("数据库文件路径：{}", dbPath);

        return dataSource;
    }

    /**
     * 初始化数据库表
     * 1. 如果数据库文件不存在，创建文件并执行建表语句
     * 2. 如果数据库文件存在，检查表是否存在，不存在则创建
     */
    private void initDatabaseTables(DataSource dataSource) {
        try (Connection conn = dataSource.getConnection()) {
            // 检查表是否存在
            if (!tablesExist(conn)) {
                // 创建表
                ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
                populator.addScript(new ClassPathResource("schema.sql"));
                // 执行SQL脚本
                populator.execute(dataSource);
            }
        } catch (SQLException e) {
            // 数据库操作异常
            throw new RuntimeException("初始化数据库表失败", e);
        }
    }

    /**
     * 检查必要的表是否都存在
     * @return true:表都存在 false:有表不存在
     */
    private boolean tablesExist(Connection conn) throws SQLException {
        // 使用try-with-resources同时管理Statement和ResultSet
        try (Statement stmt = conn.createStatement();
             ResultSet rs1 = stmt.executeQuery(
                     "SELECT name FROM sqlite_master WHERE type='table' AND name='file_info'");
             ResultSet rs2 = stmt.executeQuery(
                     "SELECT name FROM sqlite_master WHERE type='table' AND name='chunk_info'")
        ) {
            // 检查两个表是否都存在
            boolean fileInfoExists = rs1.next();
            boolean chunkInfoExists = rs2.next();
            
            // 两个表都存在才返回true
            return fileInfoExists && chunkInfoExists;
        }
    }
}
