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
}
