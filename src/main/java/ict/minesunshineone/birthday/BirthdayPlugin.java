package ict.minesunshineone.birthday;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.FireworkEffect;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;

public class BirthdayPlugin extends JavaPlugin {

    private FileConfiguration config;
    private PlayerDataManager playerDataManager;

    @Override
    public void onEnable() {
        // 保存默认配置
        saveDefaultConfig();
        config = getConfig();

        // 初始化 PlayerDataManager
        playerDataManager = new PlayerDataManager(this);

        // 注册事件监听器
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);

        // 注册命令
        var birthdayCommand = getCommand("birthday");
        if (birthdayCommand != null) {
            birthdayCommand.setExecutor(new BirthdayCommand(this));
        } else {
            getLogger().severe("无法注册 birthday 命令!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        getLogger().info("Birthday Plugin has been enabled!");

        // 注册 PAPI 扩展
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new BirthdayPlaceholder(this).register();
            getLogger().info("PlaceholderAPI 扩展注册成功!");
        }
    }

    @Override
    public void onDisable() {
        getLogger().info("Birthday Plugin has been disabled!");
    }

    public void celebrateBirthday(Player player) {
        player.showTitle(Title.title(
                Component.text("生日快乐！").color(NamedTextColor.GOLD).decorate(TextDecoration.BOLD),
                Component.text("祝你生日快乐！").color(NamedTextColor.YELLOW),
                Title.Times.times(Duration.ofSeconds(1), Duration.ofSeconds(5), Duration.ofSeconds(1))
        ));
        setBirthdayPrefix(player);

        Bukkit.broadcast(Component.text("今天是 ")
                .color(NamedTextColor.YELLOW)
                .append(Component.text(player.getName())
                        .color(NamedTextColor.GOLD))
                .append(Component.text(" 的生日！")
                        .color(NamedTextColor.YELLOW))
                .append(Component.text(" (发送\"生日快乐\"可以获得蛋糕哦)")
                        .color(NamedTextColor.GRAY)));

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

    public PlayerDataManager getPlayerDataManager() {
        return playerDataManager;
    }

    private void setBirthdayPrefix(Player player) {
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            LuckPerms luckPerms = provider.getProvider();
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());

            if (user != null) {
                // 先移除旧的生日后缀
                user.data().clear(node
                        -> node.getKey().startsWith("suffix.")
                        && node.getKey().contains("『🎂寿星』")
                );

                // 添加新后缀,设置24小时过期
                Node suffixNode = Node.builder("suffix.100.&6&l『🎂寿星』")
                        .expiry(Duration.ofHours(24))
                        .build();

                user.data().add(suffixNode);
                luckPerms.getUserManager().saveUser(user);
            }
        }
    }
}
