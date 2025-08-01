package ict.minesunshineone.birthday.database;

import ict.minesunshineone.birthday.BirthdayPlugin;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class DatabasePlayerDataManager {
    private final BirthdayPlugin plugin;
    private final DatabaseManager databaseManager;

    public DatabasePlayerDataManager(BirthdayPlugin plugin, DatabaseManager databaseManager) {
        this.plugin = plugin;
        this.databaseManager = databaseManager;
    }

    // 保存生日信息
    public void saveBirthday(Player player, int month, int day) {
        String uuid = player.getUniqueId().toString();
        String name = player.getName();
        String birthday = month + "-" + day;

        String sql = String.format("""
            INSERT INTO %splayer_data (uuid, name, birthday, updated_at) 
            VALUES (?, ?, ?, CURRENT_TIMESTAMP)
            ON DUPLICATE KEY UPDATE 
            name = VALUES(name), 
            birthday = VALUES(birthday), 
            updated_at = CURRENT_TIMESTAMP
            """, databaseManager.getTablePrefix());

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setString(1, uuid);
            stmt.setString(2, name);
            stmt.setString(3, birthday);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, String.format("保存玩家 %s 生日信息失败: %s", name, e.getMessage()), e);
        }
    }

    // 获取生日信息
    public String getBirthday(String uuid) {
        String sql = String.format("SELECT birthday FROM %splayer_data WHERE uuid = ?", databaseManager.getTablePrefix());
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setString(1, uuid);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("birthday");
                }
            }
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, String.format("获取玩家 %s 生日信息失败: %s", uuid, e.getMessage()), e);
        }
        
        return null;
    }

    // 设置上次庆祝的日期（保留兼容性）
    public void setLastCelebrated(String uuid, String date) {
        // 获取玩家名称，确保name字段有值
        String playerName = getPlayerNameByUUID(uuid);
        if (playerName == null || playerName.trim().isEmpty()) {
            // 尝试从在线玩家获取名称
            playerName = getOnlinePlayerName(uuid);
            if (playerName == null || playerName.trim().isEmpty()) {
                // 最后的后备方案：使用UUID前8位，确保不为空
                playerName = "Player_" + uuid.substring(0, 8);
            }
        }
        
        String sql = String.format("""
            INSERT INTO %splayer_data (uuid, name, last_celebrated, updated_at) 
            VALUES (?, ?, ?, CURRENT_TIMESTAMP)
            ON DUPLICATE KEY UPDATE 
            last_celebrated = VALUES(last_celebrated), 
            updated_at = CURRENT_TIMESTAMP
            """, databaseManager.getTablePrefix());

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setString(1, uuid);
            stmt.setString(2, playerName);
            stmt.setString(3, date);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, String.format("设置玩家 %s 上次庆祝日期失败: %s", uuid, e.getMessage()), e);
        }
    }

    // 获取上次庆祝的年份
    public String getLastCelebratedYear(String uuid) {
        String sql = String.format("SELECT last_celebrated_year FROM %splayer_data WHERE uuid = ?", databaseManager.getTablePrefix());
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setString(1, uuid);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("last_celebrated_year");
                }
            }
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, String.format("获取玩家 %s 上次庆祝年份失败: %s", uuid, e.getMessage()), e);
        }
        
        return null;
    }

    // 设置上次庆祝的年份
    public void setLastCelebratedYear(String uuid, String year) {
        // 获取玩家名称，确保name字段有值
        String playerName = getPlayerNameByUUID(uuid);
        if (playerName == null || playerName.trim().isEmpty()) {
            // 尝试从在线玩家获取名称
            playerName = getOnlinePlayerName(uuid);
            if (playerName == null || playerName.trim().isEmpty()) {
                // 最后的后备方案：使用UUID前8位，确保不为空
                playerName = "Player_" + uuid.substring(0, 8);
            }
        }
        
        String sql = String.format("""
            INSERT INTO %splayer_data (uuid, name, last_celebrated_year, updated_at) 
            VALUES (?, ?, ?, CURRENT_TIMESTAMP)
            ON DUPLICATE KEY UPDATE 
            last_celebrated_year = VALUES(last_celebrated_year), 
            updated_at = CURRENT_TIMESTAMP
            """, databaseManager.getTablePrefix());

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setString(1, uuid);
            stmt.setString(2, playerName);
            stmt.setString(3, year);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, String.format("设置玩家 %s 庆祝年份失败: %s", uuid, e.getMessage()), e);
        }
    }

    // 检查玩家当年是否已经庆祝过生日
    public boolean hasCelebratedThisYear(String uuid, String currentYear) {
        String lastCelebratedYear = getLastCelebratedYear(uuid);
        return currentYear.equals(lastCelebratedYear);
    }

    // 获取玩家是否已经看过生日设置GUI提示
    public boolean hasSeenBirthdayGUI(String uuid) {
        String sql = String.format("SELECT has_seen_gui FROM %splayer_data WHERE uuid = ?", databaseManager.getTablePrefix());
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setString(1, uuid);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBoolean("has_seen_gui");
                }
            }
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, String.format("获取玩家 %s GUI提示状态失败: %s", uuid, e.getMessage()), e);
        }
        
        return false;
    }

    // 设置玩家已经看过生日设置GUI提示（使用Player对象确保有名称）
    public void setHasSeenBirthdayGUI(Player player, boolean hasSeen) {
        String uuid = player.getUniqueId().toString();
        String playerName = player.getName();
        
        // 额外保护：确保玩家名称不为空
        if (playerName == null || playerName.trim().isEmpty()) {
            playerName = "Player_" + uuid.substring(0, 8);
        }
        
        String sql = String.format("""
            INSERT INTO %splayer_data (uuid, name, has_seen_gui, updated_at) 
            VALUES (?, ?, ?, CURRENT_TIMESTAMP)
            ON DUPLICATE KEY UPDATE 
            name = VALUES(name),
            has_seen_gui = VALUES(has_seen_gui), 
            updated_at = CURRENT_TIMESTAMP
            """, databaseManager.getTablePrefix());

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setString(1, uuid);
            stmt.setString(2, playerName);
            stmt.setBoolean(3, hasSeen);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, String.format("设置玩家 %s GUI提示状态失败: %s", playerName, e.getMessage()), e);
        }
    }

    // 设置玩家已经看过生日设置GUI提示
    public void setHasSeenBirthdayGUI(String uuid, boolean hasSeen) {
        // 获取玩家名称，确保name字段有值
        String playerName = getPlayerNameByUUID(uuid);
        if (playerName == null || playerName.trim().isEmpty()) {
            // 尝试从在线玩家获取名称
            playerName = getOnlinePlayerName(uuid);
            if (playerName == null || playerName.trim().isEmpty()) {
                // 最后的后备方案：使用UUID前8位，确保不为空
                playerName = "Player_" + uuid.substring(0, 8);
            }
        }
        
        String sql = String.format("""
            INSERT INTO %splayer_data (uuid, name, has_seen_gui, updated_at) 
            VALUES (?, ?, ?, CURRENT_TIMESTAMP)
            ON DUPLICATE KEY UPDATE 
            name = VALUES(name),
            has_seen_gui = VALUES(has_seen_gui), 
            updated_at = CURRENT_TIMESTAMP
            """, databaseManager.getTablePrefix());

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setString(1, uuid);
            stmt.setString(2, playerName);
            stmt.setBoolean(3, hasSeen);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, String.format("设置玩家 %s GUI提示状态失败: %s", uuid, e.getMessage()), e);
        }
    }

    // 获取今天过生日的玩家列表
    public List<String> getTodayBirthdayPlayers(String todayString) {
        List<String> birthdayPlayers = new ArrayList<>();
        String sql = String.format("SELECT name FROM %splayer_data WHERE birthday = ? AND name IS NOT NULL", databaseManager.getTablePrefix());
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setString(1, todayString);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String playerName = rs.getString("name");
                    if (playerName != null) {
                        birthdayPlayers.add(playerName);
                    }
                }
            }
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, String.format("获取今天生日玩家列表失败: %s", e.getMessage()), e);
        }
        
        return birthdayPlayers;
    }

    // 检查玩家今天是否已经送过祝福
    public boolean hasWishedToday(String uuid, String todayString) {
        String sql = String.format("SELECT 1 FROM %swishes WHERE sender_uuid = ? AND wish_date = ?", databaseManager.getTablePrefix());
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setString(1, uuid);
            stmt.setString(2, todayString);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, String.format("检查玩家 %s 祝福状态失败: %s", uuid, e.getMessage()), e);
        }
        
        return false;
    }

    // 记录玩家送出祝福
    public void recordWish(String uuid, String todayString) {
        String sql = String.format("""
            INSERT INTO %swishes (sender_uuid, wish_date) 
            VALUES (?, ?) 
            ON DUPLICATE KEY UPDATE created_at = CURRENT_TIMESTAMP
            """, databaseManager.getTablePrefix());

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setString(1, uuid);
            stmt.setString(2, todayString);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, String.format("记录玩家 %s 祝福失败: %s", uuid, e.getMessage()), e);
        }
    }

    // 通过玩家名称查找UUID（用于管理员命令）
    public String getUUIDByName(String playerName) {
        String sql = String.format("SELECT uuid FROM %splayer_data WHERE name = ? ORDER BY updated_at DESC LIMIT 1", databaseManager.getTablePrefix());
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setString(1, playerName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("uuid");
                }
            }
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, String.format("通过名称 %s 查找UUID失败: %s", playerName, e.getMessage()), e);
        }
        
        return null;
    }

    // 更新玩家名称（UUID不变时同步名称变化）
    public void updatePlayerName(String uuid, String newName) {
        String sql = String.format("""
            INSERT INTO %splayer_data (uuid, name, updated_at) 
            VALUES (?, ?, CURRENT_TIMESTAMP)
            ON DUPLICATE KEY UPDATE 
            name = VALUES(name), 
            updated_at = CURRENT_TIMESTAMP
            """, databaseManager.getTablePrefix());

        try (Connection connection = databaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setString(1, uuid);
            stmt.setString(2, newName);
            stmt.executeUpdate();
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, String.format("更新玩家 %s 名称失败: %s", newName, e.getMessage()), e);
        }
    }

    // 通过UUID获取玩家名称的辅助方法
    private String getPlayerNameByUUID(String uuid) {
        String sql = String.format("SELECT name FROM %splayer_data WHERE uuid = ?", databaseManager.getTablePrefix());
        
        try (Connection connection = databaseManager.getConnection();
             PreparedStatement stmt = connection.prepareStatement(sql)) {
            
            stmt.setString(1, uuid);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getString("name");
                }
            }
            
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, String.format("通过UUID %s 获取玩家名称失败: %s", uuid, e.getMessage()), e);
        }
        
        return null;
    }

    // 尝试从在线玩家获取名称
    private String getOnlinePlayerName(String uuid) {
        try {
            org.bukkit.entity.Player player = plugin.getServer().getPlayer(java.util.UUID.fromString(uuid));
            return player != null ? player.getName() : null;
        } catch (Exception e) {
            return null;
        }
    }
}
