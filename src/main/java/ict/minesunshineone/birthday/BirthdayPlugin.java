package ict.minesunshineone.birthday;

import java.io.File;
import java.time.Duration;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.java.JavaPlugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;

public class BirthdayPlugin extends JavaPlugin {

    private FileConfiguration config;
    private PlayerDataManager playerDataManager;

    @Override
    public void onEnable() {
        // 保存默认配置
        saveDefaultConfig();
        config = getConfig();

        // 注册事件监听器
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        // 启动生日检查任务
        startBirthdayCheckTask();

        // 注册命令
        var birthdayCommand = getCommand("birthday");
        if (birthdayCommand != null) {
            birthdayCommand.setExecutor(new BirthdayCommand(this));
        } else {
            getLogger().severe("无法注册 birthday 命令!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        playerDataManager = new PlayerDataManager(this);

        getLogger().info("Birthday Plugin has been enabled!");
    }

    @Override
    public void onDisable() {
        getLogger().info("Birthday Plugin has been disabled!");
    }

    private void startBirthdayCheckTask() {
        long checkInterval = config.getLong("settings.check-interval", 72000L);
        getServer().getAsyncScheduler().runAtFixedRate(this,
                task -> {
                    checkBirthdays();
                    checkUpcomingBirthdays();
                },
                0L,
                checkInterval * 50L, // 转换 ticks 到毫秒
                TimeUnit.MILLISECONDS
        );
    }

    private void checkBirthdays() {
        Calendar today = Calendar.getInstance();
        int currentMonth = today.get(Calendar.MONTH) + 1;
        int currentDay = today.get(Calendar.DAY_OF_MONTH);
        String todayString = currentMonth + "-" + currentDay;

        File playerDataFolder = new File(getDataFolder(), "player_data");
        File[] playerFiles = playerDataFolder.listFiles((dir, name) -> name.endsWith(".yml"));

        if (playerFiles != null) {
            for (File file : playerFiles) {
                String uuid = file.getName().replace(".yml", "");
                YamlConfiguration playerData = playerDataManager.getPlayerData(uuid);
                String birthdayString = playerData.getString("birthday");
                String playerName = playerData.getString("name");

                if (birthdayString != null && birthdayString.equals(todayString)) {
                    broadcastBirthdayMessage(playerName);
                    Player player = Bukkit.getPlayer(UUID.fromString(uuid));
                    if (player != null && player.isOnline()) {
                        celebrateBirthday(player);
                    }
                }
            }
        }
    }

    private void broadcastBirthdayMessage(String playerName) {
        Component message = Component.text("今天是 ")
                .color(NamedTextColor.GOLD)
                .decorate(TextDecoration.BOLD)
                .append(Component.text(playerName))
                .append(Component.text(" 的生日！"));
        Bukkit.broadcast(message);
    }

    public void celebrateBirthday(Player player) {
        player.showTitle(Title.title(
                Component.text("生日快乐！").color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD),
                Component.text("祝你生日快乐！").color(NamedTextColor.YELLOW),
                Title.Times.times(Duration.ofSeconds(1), Duration.ofSeconds(5), Duration.ofSeconds(1))
        ));

        spawnBirthdayFireworks(player);
        playBirthdayEffects(player);
    }

    private void spawnBirthdayFireworks(Player player) {
        Location loc = player.getLocation();
        getServer().getRegionScheduler().execute(this, loc, () -> {
            int count = config.getInt("firework.count", 5);

            for (int i = 0; i < count; i++) {
                final int delay = (i + 1) * Math.max(1, config.getInt("firework.launch-interval", 10));
                getServer().getRegionScheduler().runDelayed(this, loc, (task) -> {
                    Firework firework = (Firework) loc.getWorld().spawnEntity(loc, EntityType.FIREWORK);
                    FireworkMeta meta = firework.getFireworkMeta();

                    meta.addEffect(FireworkEffect.builder()
                            .withColor(Color.RED, Color.YELLOW, Color.BLUE)
                            .withFade(Color.fromRGB(255, 192, 203))
                            .with(FireworkEffect.Type.BALL_LARGE)
                            .trail(config.getBoolean("firework.trail", true))
                            .flicker(config.getBoolean("firework.flicker", true))
                            .build());

                    meta.setPower(config.getInt("firework.power", 2));
                    firework.setFireworkMeta(meta);
                }, delay);
            }
        });
    }

    private void playBirthdayEffects(Player player) {
        Location loc = player.getLocation();
        int duration = config.getInt("effects.duration", 100);
        long interval = config.getLong("effects.particle.interval", 2L);

        AtomicInteger tick = new AtomicInteger(0);
        getServer().getRegionScheduler().runAtFixedRate(this, loc, (task) -> {
            if (tick.get() >= duration) {
                task.cancel();
                return;
            }

            if (config.getBoolean("effects.particle.enabled", true)) {
                List<String> particleTypes = config.getStringList("effects.particle.types");
                double radius = config.getDouble("effects.particle.radius", 1.5);
                int count = config.getInt("effects.particle.count", 1);
                double height = Math.sin(tick.get() * Math.PI / 40) * 0.5 + 1.0;

                for (String particleType : particleTypes) {
                    try {
                        Particle particle = Particle.valueOf(particleType);
                        for (int i = 0; i < 5; i++) {
                            double angle = 2 * Math.PI * i / 5 + (tick.get() * Math.PI / 20);
                            Location particleLoc = loc.clone().add(
                                    radius * Math.cos(angle),
                                    height,
                                    radius * Math.sin(angle)
                            );
                            player.getWorld().spawnParticle(particle, particleLoc, count, 0, 0, 0, 0);
                        }
                    } catch (IllegalArgumentException e) {
                        getLogger().warning(String.format("无效的粒子类型: %s", particleType));
                    }
                }
            }

            tick.incrementAndGet();
        }, 1L, interval);
    }

    public void reloadBirthdayConfig() {
        try {
            // 重新加载默认配置
            reloadConfig();
            config = getConfig();

            getLogger().info("配置重载成功！");
        } catch (Exception e) {
            getLogger().severe(String.format("重载配置时发生错误: %s", e.getMessage()));
        }
    }

    private void checkUpcomingBirthdays() {
        Calendar today = Calendar.getInstance();
        int currentMonth = today.get(Calendar.MONTH) + 1;
        int currentDay = today.get(Calendar.DAY_OF_MONTH);
        int reminderDays = config.getInt("reminder.advance-days", 3);
        boolean broadcastEnabled = config.getBoolean("reminder.broadcast", false);
        String reminderMessage = config.getString("reminder.message", "再过%days%天就是 %player% 的生日了！");

        File playerDataFolder = new File(getDataFolder(), "player_data");
        if (!playerDataFolder.exists() || !playerDataFolder.isDirectory()) {
            return;
        }

        File[] playerFiles = playerDataFolder.listFiles((dir, name) -> name.endsWith(".yml"));
        if (playerFiles == null) {
            return;
        }

        for (File file : playerFiles) {
            String uuid = file.getName().replace(".yml", "");
            YamlConfiguration playerData = YamlConfiguration.loadConfiguration(file);
            String birthdayString = playerData.getString("birthday");
            String playerName = playerData.getString("name");

            if (birthdayString != null && playerName != null) {
                try {
                    String[] parts = birthdayString.split("-");
                    int month = Integer.parseInt(parts[0]);
                    int day = Integer.parseInt(parts[1]);

                    int daysUntil = calculateDaysUntilBirthday(currentMonth, currentDay, month, day);

                    if (daysUntil == reminderDays) {
                        Player player = Bukkit.getPlayer(UUID.fromString(uuid));
                        if (player != null && player.isOnline()) {
                            String message = (reminderMessage != null ? reminderMessage : "再过%days%天就是 %player% 的生日了！")
                                    .replace("%days%", String.valueOf(reminderDays))
                                    .replace("%player%", playerName);

                            if (broadcastEnabled) {
                                Bukkit.broadcast(Component.text(message).color(NamedTextColor.YELLOW));
                            } else {
                                player.sendMessage(Component.text(message).color(NamedTextColor.YELLOW));
                            }
                        }
                    }
                } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
                    getLogger().warning(String.format("检查生日提醒时发生错误 (玩家: %s): %s", playerName, e.getMessage()));
                }
            }
        }
    }

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    private int calculateDaysUntilBirthday(int currentMonth, int currentDay, int birthMonth, int birthDay) {
        Calendar today = Calendar.getInstance();
        today.set(Calendar.MONTH, currentMonth - 1);
        today.set(Calendar.DAY_OF_MONTH, currentDay);

        Calendar birthday = Calendar.getInstance();
        birthday.set(Calendar.MONTH, birthMonth - 1);
        birthday.set(Calendar.DAY_OF_MONTH, birthDay);

        if (birthday.before(today)) {
            birthday.add(Calendar.YEAR, 1);
        }

        return (int) ((birthday.getTimeInMillis() - today.getTimeInMillis()) / (1000 * 60 * 60 * 24));
    }
}
