package ict.minesunshineone.birthday.data;

import org.bukkit.entity.Player;

import java.util.List;

/**
 * 数据管理器接口，支持文件和数据库两种存储方式
 */
public interface DataManager {
    
    // 生日相关方法
    void saveBirthday(Player player, int month, int day);
    String getBirthday(String uuid);
    
    // 庆祝记录相关方法
    void setLastCelebrated(String uuid, String date);
    String getLastCelebratedYear(String uuid);
    void setLastCelebratedYear(String uuid, String year);
    boolean hasCelebratedThisYear(String uuid, String currentYear);
    
    // GUI提示相关方法
    boolean hasSeenBirthdayGUI(String uuid);
    void setHasSeenBirthdayGUI(String uuid, boolean hasSeen);
    
    // 祝福相关方法
    List<String> getTodayBirthdayPlayers(String todayString);
    boolean hasWishedToday(String uuid, String todayString);
    void recordWish(String uuid, String todayString);
    
    // 辅助方法
    String getUUIDByName(String playerName);
    void updatePlayerName(String uuid, String newName);
    
    // 数据迁移方法
    void migrateFromFile(String uuid, org.bukkit.configuration.file.YamlConfiguration oldData);
}
