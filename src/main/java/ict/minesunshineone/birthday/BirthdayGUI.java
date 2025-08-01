package ict.minesunshineone.birthday;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;

public class BirthdayGUI {

    private final BirthdayPlugin plugin;
    private final Map<UUID, Integer> selectedMonths = new HashMap<>();
    private final Map<UUID, UUID> modifyTargets = new HashMap<>();

    public BirthdayGUI(BirthdayPlugin plugin) {
        this.plugin = plugin;
    }

    public void openBirthdayGUI(Player player, UUID targetUUID) {
        Player targetPlayer = Bukkit.getPlayer(targetUUID);
        if (targetPlayer != null) {
            modifyTargets.put(player.getUniqueId(), targetUUID);
            plugin.getServer().getRegionScheduler().execute(plugin, player.getLocation(), () -> {
                Inventory gui = Bukkit.createInventory(null, 36,
                        Component.text("修改 " + targetPlayer.getName() + " 的生日月份").color(NamedTextColor.GOLD));

                // 添加装饰性边框
                ItemStack border = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
                ItemMeta borderMeta = border.getItemMeta();
                borderMeta.displayName(Component.text(" "));
                border.setItemMeta(borderMeta);

                // 设置边框
                for (int i = 0; i < 36; i++) {
                    if (i < 9 || i > 26 || i % 9 == 0 || i % 9 == 8) {
                        gui.setItem(i, border);
                    }
                }

                // 添加月份选择按钮
                for (int month = 1; month <= 12; month++) {
                    ItemStack monthItem = new ItemStack(Material.PAPER);
                    ItemMeta meta = monthItem.getItemMeta();
                    meta.displayName(Component.text(month + "月")
                            .color(NamedTextColor.GOLD)
                            .decorate(TextDecoration.BOLD));

                    List<Component> lore = new ArrayList<>();
                    lore.add(Component.text("点击选择 " + month + " 月")
                            .color(NamedTextColor.GRAY));
                    meta.lore(lore);
                    monthItem.setItemMeta(meta);

                    // 计算位置 (2x6 布局)
                    int row = (month - 1) / 6;
                    int col = (month - 1) % 6;
                    if (row == 1) { // 如果是第二行(7-12月)
                        col += 1; // 向右移动一列
                    }
                    int slot = 10 + col + (row * 9);
                    gui.setItem(slot, monthItem);
                }

                player.openInventory(gui);
            });
        } else {
            player.sendMessage(Component.text("目标玩家必须在线！").color(NamedTextColor.RED));
        }
    }

    public void openBirthdayGUI(Player player) {
        plugin.getServer().getRegionScheduler().execute(plugin, player.getLocation(), () -> {
            Inventory gui = Bukkit.createInventory(null, 36,
                    Component.text("请选择你的生日月份").color(NamedTextColor.GOLD));

            // 添加装饰性边框
            ItemStack border = new ItemStack(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
            ItemMeta borderMeta = border.getItemMeta();
            borderMeta.displayName(Component.text(" "));
            border.setItemMeta(borderMeta);

            // 设置边框
            for (int i = 0; i < 36; i++) {
                if (i < 9 || i > 26 || i % 9 == 0 || i % 9 == 8) {
                    gui.setItem(i, border);
                }
            }

            // 添加月份选择按钮
            for (int month = 1; month <= 12; month++) {
                ItemStack monthItem = new ItemStack(Material.PAPER);
                ItemMeta meta = monthItem.getItemMeta();
                meta.displayName(Component.text(month + "月")
                        .color(NamedTextColor.GOLD)
                        .decorate(TextDecoration.BOLD));

                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("点击选择 " + month + " 月")
                        .color(NamedTextColor.GRAY));
                meta.lore(lore);
                monthItem.setItemMeta(meta);

                // 计算位置 (2x6 布局)
                int row = (month - 1) / 6;
                int col = (month - 1) % 6;
                if (row == 1) { // 如果是第二行(7-12月)
                    col += 1; // 向右移动一列
                }
                int slot = 10 + col + (row * 9);
                gui.setItem(slot, monthItem);
            }

            player.openInventory(gui);
        });
    }

    public void openDayGUI(Player player, int month) {
        UUID targetUUID = modifyTargets.getOrDefault(player.getUniqueId(), player.getUniqueId());
        Player targetPlayer = Bukkit.getPlayer(targetUUID);
        String title = targetPlayer != null && !targetPlayer.equals(player)
                ? "修改 " + targetPlayer.getName() + " 的生日日期"
                : "请选择日期";

        selectedMonths.put(player.getUniqueId(), month);
        plugin.getServer().getRegionScheduler().execute(plugin, player.getLocation(), () -> {
            Inventory gui = Bukkit.createInventory(null, 54,
                    Component.text(title).color(NamedTextColor.GOLD));

            // 添加装饰性边框
            ItemStack border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            ItemMeta borderMeta = border.getItemMeta();
            borderMeta.displayName(Component.text(" "));
            border.setItemMeta(borderMeta);

            // 设置边框
            for (int i = 0; i < 54; i++) {
                if (i < 9 || i > 44 || i % 9 == 0 || i % 9 == 8) {
                    gui.setItem(i, border);
                }
            }

            int daysInMonth = getDaysInMonth(month);
            for (int day = 1; day <= daysInMonth; day++) {
                ItemStack dayItem = new ItemStack(Material.PAPER);
                ItemMeta meta = dayItem.getItemMeta();
                meta.displayName(Component.text(day + "日")
                        .color(NamedTextColor.GOLD)
                        .decorate(TextDecoration.BOLD));
                List<Component> lore = new ArrayList<>();
                lore.add(Component.text("点击选择 " + day + " 日")
                        .color(NamedTextColor.GRAY));
                meta.lore(lore);
                dayItem.setItemMeta(meta);

                // 计算位置
                int slot = 10 + ((day - 1) % 7) + ((day - 1) / 7 * 9);
                gui.setItem(slot, dayItem);
            }

            player.openInventory(gui);
        });
    }

    public int getDaysInMonth(int month) {
        return switch (month) {
            case 2 ->
                28; // 2025年2月是28天
            case 4, 6, 9, 11 ->
                30;
            default ->
                31;
        };
    }

    public void saveBirthday(Player player, int month, int day) {
        try {
            UUID targetUUID = modifyTargets.getOrDefault(player.getUniqueId(), player.getUniqueId());
            Player targetPlayer = Bukkit.getPlayer(targetUUID);

            if (!player.getUniqueId().equals(targetUUID) && !player.hasPermission("birthday.modify")) {
                player.sendMessage(Component.text("你没有权限修改其他玩家的生日信息！").color(NamedTextColor.RED));
                return;
            }

            if (!isValidDate(month, day)) {
                player.sendMessage(Component.text("无效的日期！请重新选择。").color(NamedTextColor.RED));
                openBirthdayGUI(player);
                return;
            }

            // 使用目标玩家的UUID保存生日信息
            if (targetPlayer != null) {
                String uuid = targetPlayer.getUniqueId().toString();
                String existingBirthday = plugin.getPlayerDataManager().getBirthday(uuid);
                
                plugin.getPlayerDataManager().saveBirthday(targetPlayer, month, day);
                
                if (player.getUniqueId().equals(targetUUID)) {
                    // 玩家为自己设置生日
                    if (existingBirthday != null) {
                        player.sendMessage(Component.text("🎉 生日信息已更新！你的生日是 " + month + "月" + day + "日")
                                .color(NamedTextColor.GREEN));
                        player.sendMessage(Component.text("提示: 每年只能庆祝一次生日，即使修改生日日期")
                                .color(NamedTextColor.GRAY));
                    } else {
                        // 首次设置生日的特殊消息
                        player.sendMessage(Component.text("🎂 恭喜！生日信息设置成功！")
                                .color(NamedTextColor.GREEN).decorate(net.kyori.adventure.text.format.TextDecoration.BOLD));
                        player.sendMessage(Component.text("你的生日是 " + month + "月" + day + "日")
                                .color(NamedTextColor.GOLD));
                        player.sendMessage(Component.text("生日当天登录服务器会有惊喜哦！🎈")
                                .color(NamedTextColor.YELLOW));
                        player.sendMessage(Component.text("提示: 你可以随时使用 /birthday set 修改生日")
                                .color(NamedTextColor.GRAY));
                        
                        // 重置GUI提示状态，因为玩家已经设置了生日
                        // 注意：这里不需要重置has_seen_gui，因为玩家已经有生日了，不会再触发提醒
                    }
                } else {
                    // 管理员为其他玩家设置生日
                    player.sendMessage(Component.text("生日信息设置成功！" + targetPlayer.getName() + "的生日是 " + month + "月" + day + "日")
                            .color(NamedTextColor.GREEN));
                }
            } else {
                player.sendMessage(Component.text("目标玩家必须在线！").color(NamedTextColor.RED));
                return;
            }

            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);

            // 清除修改目标
            modifyTargets.remove(player.getUniqueId());
        } catch (Exception e) {
            player.sendMessage(Component.text("保存生日信息时发生错误，请联系管理员！").color(NamedTextColor.RED));
            plugin.getLogger().severe(String.format("保存玩家生日信息时发生错误: %s", e.getMessage()));
        }
    }

    private boolean isValidDate(int month, int day) {
        if (month < 1 || month > 12) {
            return false;
        }

        int maxDays = getDaysInMonth(month);
        return day >= 1 && day <= maxDays;
    }

    public int getSelectedMonth(Player player) {
        return selectedMonths.getOrDefault(player.getUniqueId(), 1);
    }
}
