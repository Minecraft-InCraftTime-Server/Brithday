# 🌐 生日插件跨服数据库配置指南

## 📋 功能概述

生日插件现在支持两种数据存储方式：
- **文件存储** (默认): 数据存储在本地YAML文件中
- **数据库存储**: 数据存储在MySQL数据库中，支持跨服共享

## 🗄️ 数据库设置

### 1. 准备MySQL数据库

确保你有一个MySQL数据库服务器，并创建一个数据库：

```sql
CREATE DATABASE minecraft CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'birthdayuser'@'%' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON minecraft.* TO 'birthdayuser'@'%';
FLUSH PRIVILEGES;
```

### 2. 配置插件

在 `config.yml` 中设置数据库配置：

```yaml
# 数据存储配置
storage:
  # 存储类型: FILE 或 DATABASE
  type: DATABASE
  
# 数据库配置 (当 storage.type 为 DATABASE 时使用)
database:
  host: "your-database-host"     # 数据库主机地址
  port: 3306                     # 数据库端口
  database: "minecraft"          # 数据库名称
  username: "birthdayuser"       # 数据库用户名
  password: "your_password"      # 数据库密码
  useSSL: false                  # 是否使用SSL连接
  table-prefix: "birthday_"      # 表前缀
  max-connections: 10            # 最大连接数
  connection-timeout: 5000       # 连接超时时间(毫秒)
```

### 3. 数据库表结构

插件会自动创建以下表：

```sql
-- 玩家数据表
CREATE TABLE birthday_player_data (
    uuid VARCHAR(36) PRIMARY KEY,
    name VARCHAR(16) NOT NULL,
    birthday VARCHAR(10),
    last_celebrated VARCHAR(10),
    last_celebrated_year VARCHAR(4),
    has_seen_gui BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_birthday (birthday),
    INDEX idx_name (name)
);

-- 祝福记录表
CREATE TABLE birthday_wishes (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sender_uuid VARCHAR(36) NOT NULL,
    wish_date VARCHAR(10) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY unique_wish (sender_uuid, wish_date),
    INDEX idx_wish_date (wish_date)
);
```

## 🔄 跨服部署

### 多服务器共享配置

1. **所有服务器使用相同的数据库配置**
2. **确保表前缀一致** (如都使用 `birthday_`)
3. **网络连通性** - 所有服务器都能访问数据库

### 示例部署架构

```
[服务器1] ----\
              \
[服务器2] -------> [MySQL数据库] <------- [其他服务器...]
              /
[服务器3] ----/
```

## ⚡ 性能优化

### 连接池配置

- `max-connections`: 根据服务器负载调整 (推荐5-15)
- `connection-timeout`: 网络延迟较高时增加此值

### 数据库优化

```sql
-- 为经常查询的字段添加索引
ALTER TABLE birthday_player_data ADD INDEX idx_birthday_year (birthday, last_celebrated_year);
ALTER TABLE birthday_wishes ADD INDEX idx_sender_date (sender_uuid, wish_date);
```

## 🛠️ 故障排除

### 常见问题

1. **连接失败**
   - 检查数据库服务器是否运行
   - 验证网络连接和防火墙设置
   - 确认用户名和密码正确

2. **权限问题**
   - 确保数据库用户有足够权限
   - 检查表前缀配置是否正确

3. **性能问题**
   - 增加连接池大小
   - 优化数据库索引
   - 考虑使用数据库缓存

### 日志查看

插件会在控制台输出数据库连接状态：
- `数据库连接成功！` - 表示连接正常
- `数据库连接失败` - 检查配置和网络

## 📊 数据迁移

### 从文件到数据库

1. 首先备份现有数据文件
2. 配置数据库连接
3. 重启服务器
4. 使用命令 `/birthday migrate` (即将实现)

### 监控和维护

- 定期备份数据库
- 监控连接池使用情况
- 定期清理过期的祝福记录

## 🔐 安全建议

1. **使用专用数据库用户**
   - 不要使用root用户
   - 只授予必要的权限

2. **网络安全**
   - 使用防火墙限制数据库访问
   - 考虑使用VPN连接

3. **数据加密**
   - 在生产环境中启用SSL连接
   - 定期更换数据库密码

## 📈 扩展性

当前数据库设计支持：
- ✅ 无限玩家数量
- ✅ 历史数据保留
- ✅ 跨服务器实时同步
- ✅ 高并发访问

未来可能的扩展：
- Redis缓存支持
- 分布式数据库支持
- 数据分析功能

---

## 🚀 快速开始

1. **创建数据库和用户**
2. **修改 `config.yml` 中的 `storage.type` 为 `DATABASE`**
3. **配置数据库连接信息**
4. **重启服务器**
5. **验证连接** - 查看控制台日志

设置完成后，所有服务器的生日数据将实时同步！🎉
