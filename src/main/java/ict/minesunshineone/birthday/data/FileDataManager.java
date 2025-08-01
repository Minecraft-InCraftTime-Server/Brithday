package ict.minesunshineone.birthday.data;

import ict.minesunshineone.birthday.BirthdayPlugin;
import ict.minesunshineone.birthday.PlayerDataManager;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * 文件数据管理器实现（包装原有的PlayerDataManager）
 */
public class FileDataManager implements DataManager {
    private final PlayerDataManager fileManager;
    private final BirthdayPlugin plugin;

    public FileDataManager(BirthdayPlugin plugin) {
        this.plugin = plugin;
        this.fileManager = new PlayerDataManager(plugin);
    }

    @Override
    public void saveBirthday(Player player, int month, int day) {
        fileManager.saveBirthday(player, month, day);
    }

    @Override
    public String getBirthday(String uuid) {
        return fileManager.getBirthday(uuid);
    }

    @Override
    public void setLastCelebrated(String uuid, String date) {
        fileManager.setLastCelebrated(uuid, date);
    }

    @Override
    public String getLastCelebratedYear(String uuid) {
        return fileManager.getLastCelebratedYear(uuid);
    }

    @Override
    public void setLastCelebratedYear(String uuid, String year) {
        fileManager.setLastCelebratedYear(uuid, year);
    }

    @Override
    public boolean hasCelebratedThisYear(String uuid, String currentYear) {
        return fileManager.hasCelebratedThisYear(uuid, currentYear);
    }

    @Override
    public boolean hasSeenBirthdayGUI(String uuid) {
        return fileManager.hasSeenBirthdayGUI(uuid);
    }

    @Override
    public void setHasSeenBirthdayGUI(String uuid, boolean hasSeen) {
        fileManager.setHasSeenBirthdayGUI(uuid, hasSeen);
    }

    @Override
    public List<String> getTodayBirthdayPlayers(String todayString) {
        List<String> birthdayPlayers = new ArrayList<>();
        File playerDataFolder = new File(plugin.getDataFolder(), "player_data");
        File[] playerFiles = playerDataFolder.listFiles((dir, name) -> name.endsWith(".yml"));

        if (playerFiles != null) {
            for (File file : playerFiles) {
                String uuid = file.getName().replace(".yml", "");
                YamlConfiguration playerData = fileManager.getPlayerData(uuid);
                String birthdayString = playerData.getString("birthday");

                if (birthdayString != null && birthdayString.equals(todayString)) {
                    String playerName = playerData.getString("name");
                    if (playerName != null) {
                        birthdayPlayers.add(playerName);
                    }
                }
            }
        }
        return birthdayPlayers;
    }

    @Override
    public boolean hasWishedToday(String uuid, String todayString) {
        YamlConfiguration wishData = fileManager.getPlayerData(uuid);
        String wishKey = "wishes." + todayString;
        List<String> todayWishes = wishData.getStringList(wishKey);
        return todayWishes.contains(uuid);
    }

    @Override
    public void recordWish(String uuid, String todayString) {
        YamlConfiguration wishData = fileManager.getPlayerData(uuid);
        String wishKey = "wishes." + todayString;
        List<String> todayWishes = wishData.getStringList(wishKey);
        
        if (!todayWishes.contains(uuid)) {
            todayWishes.add(uuid);
            wishData.set(wishKey, todayWishes);
            fileManager.savePlayerData(uuid, wishData);
        }
    }

    @Override
    public String getUUIDByName(String playerName) {
        File playerDataFolder = new File(plugin.getDataFolder(), "player_data");
        File[] files = playerDataFolder.listFiles();
        if (files != null) {
            for (File file : files) {
                YamlConfiguration data = YamlConfiguration.loadConfiguration(file);
                if (playerName.equals(data.getString("name"))) {
                    return file.getName().replace(".yml", "");
                }
            }
        }
        return null;
    }

    @Override
    public void updatePlayerName(String uuid, String newName) {
        YamlConfiguration playerData = fileManager.getPlayerData(uuid);
        playerData.set("name", newName);
        fileManager.savePlayerData(uuid, playerData);
    }

    @Override
    public void migrateFromFile(String uuid, YamlConfiguration oldData) {
        // 文件到文件不需要迁移
    }

    // 提供访问原始PlayerDataManager的方法
    public PlayerDataManager getFileManager() {
        return fileManager;
    }
}
