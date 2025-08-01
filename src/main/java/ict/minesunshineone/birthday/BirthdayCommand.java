package ict.minesunshineone.birthday;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class BirthdayCommand implements CommandExecutor, TabCompleter {

    private final BirthdayPlugin plugin;
    private final BirthdayGUI gui;

    public BirthdayCommand(BirthdayPlugin plugin) {
        this.plugin = plugin;
        this.gui = new BirthdayGUI(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender == null || !(sender instanceof Player)) {
            if (sender != null) {
                sender.sendMessage(Component.text("此命令只能由玩家使用！").color(NamedTextColor.RED));
            }
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            showHelpMessage(player);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "set" -> {
                // 移除生日已设置的限制，允许玩家随时修改
                gui.openBirthdayGUI(player);
                return true;
            }
            case "check" -> {
                showBirthdayInfo(player);
                return true;
            }
            case "modify" -> {
                if (player.hasPermission("birthday.modify")) {
                    if (args.length < 4) {
                        player.sendMessage(Component.text("用法: /birthday modify <玩家名> <月> <日>").color(NamedTextColor.RED));
                        return true;
                    }
                    String targetName = args[1];

                    try {
                        int month = Integer.parseInt(args[2]);
                        int day = Integer.parseInt(args[3]);

                        if (!isValidDate(month, day)) {
                            player.sendMessage(Component.text("无效的日期！").color(NamedTextColor.RED));
                            return true;
                        }

                        // 获取在线玩家或从历史数据中获取UUID
                        Player targetPlayer = Bukkit.getPlayer(targetName);
                        String targetUUID;

                        if (targetPlayer != null) {
                            targetUUID = targetPlayer.getUniqueId().toString();
                        } else {
                            // 检查离线玩家数据
                            File playerDataFolder = new File(plugin.getDataFolder(), "player_data");
                            File[] files = playerDataFolder.listFiles();
                            if (files != null) {
                                targetUUID = null;
                                for (File file : files) {
                                    YamlConfiguration data = YamlConfiguration.loadConfiguration(file);
                                    if (targetName.equals(data.getString("name"))) {
                                        targetUUID = file.getName().replace(".yml", "");
                                        break;
                                    }
                                }
                                if (targetUUID == null) {
                                    player.sendMessage(Component.text("找不到玩家: " + targetName).color(NamedTextColor.RED));
                                    return true;
                                }
                            } else {
                                player.sendMessage(Component.text("无法读取玩家数据！").color(NamedTextColor.RED));
                                return true;
                            }
                        }

                        // 保存生日信息
                        YamlConfiguration playerData = plugin.getPlayerDataManager().getPlayerData(targetUUID);
                        playerData.set("name", targetName);
                        playerData.set("birthday", month + "-" + day);
                        plugin.getPlayerDataManager().savePlayerData(targetUUID, playerData);

                        player.sendMessage(Component.text("已将 " + targetName + " 的生日设置为 " + month + "月" + day + "日")
                                .color(NamedTextColor.GREEN));

                    } catch (NumberFormatException e) {
                        player.sendMessage(Component.text("月份和日期必须是数字！").color(NamedTextColor.RED));
                    }
                } else {
                    player.sendMessage(Component.text("你没有权限修改生日信息！").color(NamedTextColor.RED));
                }
                return true;
            }
            case "reload" -> {
                if (player.hasPermission("birthday.admin")) {
                    plugin.reloadConfig();
                    player.sendMessage(Component.text("配置重载成功！").color(NamedTextColor.GREEN));
                } else {
                    player.sendMessage(Component.text("你没有权限重载配置！").color(NamedTextColor.RED));
                }
                return true;
            }
            default -> {
                showHelpMessage(player);
                return true;
            }
        }
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("birthday.modify")) {
            return Collections.emptyList();
        }

        return switch (args.length) {
            case 1 ->
                List.of("set", "check", "modify", "reload").stream()
                .filter(s -> s.startsWith(args[0].toLowerCase()))
                .collect(Collectors.toList());
            case 2 ->
                args[0].equalsIgnoreCase("modify")
                ? Bukkit.getOnlinePlayers().stream()
                .map(Player::getName)
                .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                .collect(Collectors.toList()) : Collections.emptyList();
            case 3 ->
                args[0].equalsIgnoreCase("modify")
                ? IntStream.rangeClosed(1, 12)
                .mapToObj(String::valueOf)
                .filter(s -> s.startsWith(args[2]))
                .collect(Collectors.toList()) : Collections.emptyList();
            case 4 ->
                args[0].equalsIgnoreCase("modify")
                ? IntStream.rangeClosed(1, 31)
                .mapToObj(String::valueOf)
                .filter(s -> s.startsWith(args[3]))
                .collect(Collectors.toList()) : Collections.emptyList();
            default ->
                Collections.emptyList();
        };
    }

    private void showHelpMessage(Player player) {
        player.sendMessage(Component.text("=== 生日系统帮助 ===").color(NamedTextColor.GOLD));
        player.sendMessage(Component.text("/birthday set - 设置或修改生日").color(NamedTextColor.YELLOW));
        player.sendMessage(Component.text("/birthday check - 查看生日信息").color(NamedTextColor.YELLOW));
        if (player.hasPermission("birthday.modify")) {
            player.sendMessage(Component.text("/birthday modify - 修改其他玩家生日信息").color(NamedTextColor.YELLOW));
        }
        if (player.hasPermission("birthday.admin")) {
            player.sendMessage(Component.text("/birthday reload - 重载配置").color(NamedTextColor.YELLOW));
        }
    }

    private void showBirthdayInfo(Player player) {
        try {
            String uuid = player.getUniqueId().toString();
            String birthdayString = plugin.getPlayerDataManager().getBirthday(uuid);

            if (birthdayString != null) {
                String[] parts = birthdayString.split("-");
                player.sendMessage(Component.text("你的生日是: " + parts[0] + "月" + parts[1] + "日")
                        .color(NamedTextColor.GREEN));
                player.sendMessage(Component.text("提示: 你可以随时使用 /birthday set 修改生日，但每年只能庆祝一次")
                        .color(NamedTextColor.GRAY));
            } else {
                player.sendMessage(Component.text("你还没有设置生日信息！")
                        .color(NamedTextColor.YELLOW));
                gui.openBirthdayGUI(player);
            }
        } catch (Exception e) {
            player.sendMessage(Component.text("获取生日信息时发生错误！")
                    .color(NamedTextColor.RED));
            plugin.getLogger().severe(String.format("获取玩家生日信息时发生错误: %s", e.getMessage()));
        }
    }

    private boolean isValidDate(int month, int day) {
        if (month < 1 || month > 12) {
            return false;
        }
        return switch (month) {
            case 2 ->
                day >= 1 && day <= 28;
            case 4, 6, 9, 11 ->
                day >= 1 && day <= 30;
            default ->
                day >= 1 && day <= 31;
        };
    }
}
