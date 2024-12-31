package ict.minesunshineone.birthday;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;

public class BirthdayCommand implements CommandExecutor {

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
                gui.openBirthdayGUI(player);
                return true;
            }
            case "check" -> {
                showBirthdayInfo(player);
                return true;
            }
            case "modify" -> {
                if (player.hasPermission("birthday.modify")) {
                    gui.openBirthdayGUI(player);
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

    private void showHelpMessage(Player player) {
        player.sendMessage(Component.text("=== 生日系统帮助 ===").color(NamedTextColor.GOLD));
        player.sendMessage(Component.text("/birthday set - 设置生日").color(NamedTextColor.YELLOW));
        player.sendMessage(Component.text("/birthday check - 查看生日信息").color(NamedTextColor.YELLOW));
        if (player.hasPermission("birthday.modify")) {
            player.sendMessage(Component.text("/birthday modify - 修改生日信息").color(NamedTextColor.YELLOW));
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
}
