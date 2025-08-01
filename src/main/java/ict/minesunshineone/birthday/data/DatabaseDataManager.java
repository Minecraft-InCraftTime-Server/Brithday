package ict.minesunshineone.birthday.data;

import ict.minesunshineone.birthday.BirthdayPlugin;
import ict.minesunshineone.birthday.database.DatabaseManager;
import ict.minesunshineone.birthday.database.DatabasePlayerDataManager;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * 数据库数据管理器实现
 */
public class DatabaseDataManager implements DataManager {
    private final DatabasePlayerDataManager dbManager;

    public DatabaseDataManager(BirthdayPlugin plugin, DatabaseManager databaseManager) {
        this.dbManager = new DatabasePlayerDataManager(plugin, databaseManager);
    }

    @Override
    public void saveBirthday(Player player, int month, int day) {
        dbManager.saveBirthday(player, month, day);
    }

    @Override
    public String getBirthday(String uuid) {
        return dbManager.getBirthday(uuid);
    }

    @Override
    public void setLastCelebrated(String uuid, String date) {
        dbManager.setLastCelebrated(uuid, date);
    }

    @Override
    public String getLastCelebratedYear(String uuid) {
        return dbManager.getLastCelebratedYear(uuid);
    }

    @Override
    public void setLastCelebratedYear(String uuid, String year) {
        dbManager.setLastCelebratedYear(uuid, year);
    }

    @Override
    public boolean hasCelebratedThisYear(String uuid, String currentYear) {
        return dbManager.hasCelebratedThisYear(uuid, currentYear);
    }

    @Override
    public boolean hasSeenBirthdayGUI(String uuid) {
        return dbManager.hasSeenBirthdayGUI(uuid);
    }

    @Override
    public void setHasSeenBirthdayGUI(String uuid, boolean hasSeen) {
        dbManager.setHasSeenBirthdayGUI(uuid, hasSeen);
    }

    @Override
    public List<String> getTodayBirthdayPlayers(String todayString) {
        return dbManager.getTodayBirthdayPlayers(todayString);
    }

    @Override
    public boolean hasWishedToday(String uuid, String todayString) {
        return dbManager.hasWishedToday(uuid, todayString);
    }

    @Override
    public void recordWish(String uuid, String todayString) {
        dbManager.recordWish(uuid, todayString);
    }

    @Override
    public String getUUIDByName(String playerName) {
        return dbManager.getUUIDByName(playerName);
    }

    @Override
    public void updatePlayerName(String uuid, String newName) {
        dbManager.updatePlayerName(uuid, newName);
    }

    @Override
    public void migrateFromFile(String uuid, YamlConfiguration oldData) {
        // 迁移逻辑将在文件数据管理器中实现
    }
}
