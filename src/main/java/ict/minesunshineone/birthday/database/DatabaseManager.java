package ict.minesunshineone.birthday.database;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import ict.minesunshineone.birthday.BirthdayPlugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;

public class DatabaseManager {
    private final BirthdayPlugin plugin;
    private final DatabaseConfig config;
    private HikariDataSource dataSource;
    private boolean connected = false;

    public DatabaseManager(BirthdayPlugin plugin, DatabaseConfig config) {
        this.plugin = plugin;
        this.config = config;
    }

    public boolean connect() {
        try {
            HikariConfig hikariConfig = new HikariConfig();
            hikariConfig.setJdbcUrl(config.getJdbcUrl());
            hikariConfig.setUsername(config.getUsername());
            hikariConfig.setPassword(config.getPassword());
            hikariConfig.setMaximumPoolSize(config.getMaxConnections());
            hikariConfig.setConnectionTimeout(config.getConnectionTimeout());
            hikariConfig.setIdleTimeout(600000);
            hikariConfig.setMaxLifetime(1800000);
            hikariConfig.setLeakDetectionThreshold(60000);

            this.dataSource = new HikariDataSource(hikariConfig);
            
            // 测试连接
            try (Connection connection = dataSource.getConnection()) {
                this.connected = true;
                plugin.getLogger().info("数据库连接成功！");
                createTables();
                return true;
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "数据库连接失败: " + e.getMessage(), e);
            this.connected = false;
            return false;
        }
    }

    public void disconnect() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            connected = false;
            plugin.getLogger().info("数据库连接已关闭");
        }
    }

    public Connection getConnection() throws SQLException {
        if (!connected || dataSource == null) {
            throw new SQLException("数据库未连接");
        }
        return dataSource.getConnection();
    }

    public boolean isConnected() {
        return connected && dataSource != null && !dataSource.isClosed();
    }

    private void createTables() {
        String createPlayerDataTable = String.format("""
            CREATE TABLE IF NOT EXISTS %splayer_data (
                uuid VARCHAR(36) PRIMARY KEY,
                name VARCHAR(16) NOT NULL DEFAULT 'Unknown',
                birthday VARCHAR(10),
                last_celebrated VARCHAR(10),
                last_celebrated_year VARCHAR(4),
                has_seen_gui BOOLEAN DEFAULT FALSE,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                INDEX idx_birthday (birthday),
                INDEX idx_name (name)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """, config.getTablePrefix());

        String createWishesTable = String.format("""
            CREATE TABLE IF NOT EXISTS %swishes (
                id INT AUTO_INCREMENT PRIMARY KEY,
                sender_uuid VARCHAR(36) NOT NULL,
                wish_date VARCHAR(10) NOT NULL,
                created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                UNIQUE KEY unique_wish (sender_uuid, wish_date),
                INDEX idx_wish_date (wish_date)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
            """, config.getTablePrefix());

        try (Connection connection = getConnection()) {
            try (PreparedStatement stmt = connection.prepareStatement(createPlayerDataTable)) {
                stmt.execute();
                plugin.getLogger().info("玩家数据表创建/检查完成");
            }
            try (PreparedStatement stmt = connection.prepareStatement(createWishesTable)) {
                stmt.execute();
                plugin.getLogger().info("祝福数据表创建/检查完成");
            }
            
            // 检查并修复现有数据库表结构
            fixTableStructure(connection);
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "创建数据表失败: " + e.getMessage(), e);
        }
    }

    public String getTablePrefix() {
        return config.getTablePrefix();
    }
    
    // 修复现有数据库表结构
    private void fixTableStructure(Connection connection) {
        try {
            // 检查 name 字段是否有默认值
            String checkSql = String.format("""
                SELECT COLUMN_DEFAULT, IS_NULLABLE 
                FROM INFORMATION_SCHEMA.COLUMNS 
                WHERE TABLE_SCHEMA = ? AND TABLE_NAME = ? AND COLUMN_NAME = 'name'
                """);
            
            try (PreparedStatement stmt = connection.prepareStatement(checkSql)) {
                stmt.setString(1, config.getDatabase());
                stmt.setString(2, config.getTablePrefix() + "player_data");
                
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        String defaultValue = rs.getString("COLUMN_DEFAULT");
                        String isNullable = rs.getString("IS_NULLABLE");
                        
                        // 如果 name 字段没有默认值且不能为空，需要修复
                        if (defaultValue == null && "NO".equals(isNullable)) {
                            String alterSql = String.format("""
                                ALTER TABLE %splayer_data 
                                MODIFY COLUMN name VARCHAR(16) NOT NULL DEFAULT 'Unknown'
                                """, config.getTablePrefix());
                            
                            try (PreparedStatement alterStmt = connection.prepareStatement(alterSql)) {
                                alterStmt.execute();
                                plugin.getLogger().info("已修复 name 字段，添加默认值 'Unknown'");
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "检查/修复表结构时出现错误: " + e.getMessage(), e);
        }
    }
}
