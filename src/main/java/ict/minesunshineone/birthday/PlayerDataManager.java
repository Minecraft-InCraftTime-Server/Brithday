package ict.minesunshineone.birthday;

import java.io.File;
import java.io.IOException;

import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

public class PlayerDataManager {

    private final BirthdayPlugin plugin;
    private final File playerDataFolder;

    public PlayerDataManager(BirthdayPlugin plugin) {
        this.plugin = plugin;
        this.playerDataFolder = new File(plugin.getDataFolder(), "player_data");
        if (!playerDataFolder.exists()) {
            playerDataFolder.mkdirs();
        }
    }

    public YamlConfiguration getPlayerData(String uuid) {
        File playerFile = new File(playerDataFolder, uuid + ".yml");
        if (!playerFile.exists()) {
            try {
                playerFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe(String.format("无法创建玩家数据文件: %s", e.getMessage()));
            }
        }
        return YamlConfiguration.loadConfiguration(playerFile);
    }

    public void savePlayerData(String uuid, YamlConfiguration config) {
        try {
            config.save(new File(playerDataFolder, uuid + ".yml"));
        } catch (IOException e) {
            plugin.getLogger().severe(String.format("无法保存玩家数据: %s", e.getMessage()));
        }
    }

    public void saveBirthday(Player player, int month, int day) {
        String uuid = player.getUniqueId().toString();
        YamlConfiguration playerData = getPlayerData(uuid);
        playerData.set("name", player.getName());
        playerData.set("birthday", month + "-" + day);
        savePlayerData(uuid, playerData);
    }

    public String getBirthday(String uuid) {
        YamlConfiguration playerData = getPlayerData(uuid);
        return playerData.getString("birthday");
    }

    public void setLastCelebrated(String uuid, String date) {
        YamlConfiguration playerData = getPlayerData(uuid);
        playerData.set("last_celebrated", date);
        savePlayerData(uuid, playerData);
    }

    // 获取玩家上次庆祝的年份
    public String getLastCelebratedYear(String uuid) {
        YamlConfiguration playerData = getPlayerData(uuid);
        return playerData.getString("last_celebrated_year");
    }

    // 设置玩家上次庆祝的年份
    public void setLastCelebratedYear(String uuid, String year) {
        YamlConfiguration playerData = getPlayerData(uuid);
        playerData.set("last_celebrated_year", year);
        savePlayerData(uuid, playerData);
    }

    // 检查玩家当年是否已经庆祝过生日
    public boolean hasCelebratedThisYear(String uuid, String currentYear) {
        String lastCelebratedYear = getLastCelebratedYear(uuid);
        return currentYear.equals(lastCelebratedYear);
    }

    // 获取玩家是否已经看过生日设置GUI提示
    public boolean hasSeenBirthdayGUI(String uuid) {
        YamlConfiguration playerData = getPlayerData(uuid);
        return playerData.getBoolean("has_seen_gui", false);
    }

    // 设置玩家已经看过生日设置GUI提示
    public void setHasSeenBirthdayGUI(String uuid, boolean hasSeen) {
        YamlConfiguration playerData = getPlayerData(uuid);
        playerData.set("has_seen_gui", hasSeen);
        savePlayerData(uuid, playerData);
    }
}
