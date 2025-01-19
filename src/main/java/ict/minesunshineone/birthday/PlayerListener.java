package ict.minesunshineone.birthday;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;

public class PlayerListener implements Listener {

    private final BirthdayPlugin plugin;
    private final BirthdayGUI birthdayGUI;

    public PlayerListener(BirthdayPlugin plugin) {
        this.plugin = plugin;
        this.birthdayGUI = new BirthdayGUI(plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String uuid = player.getUniqueId().toString();

        plugin.getServer().getRegionScheduler().runDelayed(plugin, player.getLocation(), (task) -> {
            if (plugin.getPlayerDataManager().getBirthday(uuid) == null) {
                birthdayGUI.openBirthdayGUI(player);
                // 发送提示消息
                player.sendMessage(Component.empty());
                player.sendMessage(Component.text("━━━━━━━━━━ 生日系统 ━━━━━━━━━━")
                        .color(NamedTextColor.GOLD));
                player.sendMessage(Component.text("你还没有设置生日信息！")
                        .color(NamedTextColor.YELLOW));
                player.sendMessage(Component.text("使用 /birthday set 命令设置你的生日")
                        .color(NamedTextColor.YELLOW));
                player.sendMessage(Component.text("━━━━━━━━━━━━━━━━━━━━━━")
                        .color(NamedTextColor.GOLD));
                player.sendMessage(Component.empty());

                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            } else {
                Calendar today = Calendar.getInstance();
                int currentMonth = today.get(Calendar.MONTH) + 1;
                int currentDay = today.get(Calendar.DAY_OF_MONTH);
                String todayString = currentMonth + "-" + currentDay;

                YamlConfiguration playerData = plugin.getPlayerDataManager().getPlayerData(uuid);
                String birthdayString = playerData.getString("birthday");
                String lastCelebrated = playerData.getString("last_celebrated");

                if (birthdayString != null && birthdayString.equals(todayString) && !todayString.equals(lastCelebrated)) {
                    plugin.celebrateBirthday(player);
                    plugin.getPlayerDataManager().setLastCelebrated(uuid, todayString);
                }
            }
        }, 20L);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = PlainTextComponentSerializer.plainText().serialize(event.getView().title());
        Player player = (Player) event.getWhoClicked();

        // 检查是否是修改他人生日
        if (title.startsWith("修改 ") && !player.hasPermission("birthday.modify")) {
            event.setCancelled(true);
            player.closeInventory();
            player.sendMessage(Component.text("你没有权限修改其他玩家的生日！").color(NamedTextColor.RED));
            return;
        }

        String uuid = player.getUniqueId().toString();

        // 检查玩家是否已经设置过生日且没有修改权限
        if ((title.equals("请选择你的生日月份") || title.equals("请选择日期"))
                && plugin.getPlayerDataManager().getBirthday(uuid) != null
                && !player.hasPermission("birthday.modify")) {
            event.setCancelled(true);
            player.closeInventory();
            player.sendMessage(Component.text("你已经设置过生日了！如需修改请联系管理员。")
                    .color(NamedTextColor.RED));
            player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
            return;
        }

        if (title.equals("请选择你的生日月份")) {
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem != null && clickedItem.getType() == Material.PAPER) {
                // 计算实际月份
                int slot = event.getSlot();
                int row = (slot - 10) / 9;
                int col = (slot - 10) % 9;
                int month;
                if (row == 0) {
                    month = col + 1;
                } else {
                    month = (row * 6) + (col - 1) + 1; // 减1是因为第二行向右移动了一列
                }

                if (month >= 1 && month <= 12) {
                    birthdayGUI.openDayGUI((Player) event.getWhoClicked(), month);
                }
            }
        } else if (title.equals("请选择日期")) {
            event.setCancelled(true);
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem != null && clickedItem.getType() == Material.PAPER) {
                // 计算实际日期
                int slot = event.getSlot();
                int row = (slot - 10) / 9;
                int col = (slot - 10) % 9;
                int day = row * 7 + col + 1;

                int selectedMonth = birthdayGUI.getSelectedMonth(player);
                if (day >= 1 && day <= birthdayGUI.getDaysInMonth(selectedMonth)) {
                    birthdayGUI.saveBirthday(player, selectedMonth, day);
                    player.closeInventory();
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerChat(AsyncChatEvent event) {
        Component message = event.message();
        if (PlainTextComponentSerializer.plainText().serialize(message).equals("生日快乐")) {
            plugin.getServer().getRegionScheduler().execute(plugin, event.getPlayer().getLocation(), () -> {
                checkBirthdayWish(event.getPlayer());
            });
        }
    }

    private void checkBirthdayWish(Player sender) {
        Calendar today = Calendar.getInstance();
        int currentMonth = today.get(Calendar.MONTH) + 1;
        int currentDay = today.get(Calendar.DAY_OF_MONTH);
        String todayString = currentMonth + "-" + currentDay;

        File playerDataFolder = new File(plugin.getDataFolder(), "player_data");
        File[] playerFiles = playerDataFolder.listFiles((dir, name) -> name.endsWith(".yml"));

        List<String> birthdayPlayers = new ArrayList<>();

        if (playerFiles != null) {
            for (File file : playerFiles) {
                String uuid = file.getName().replace(".yml", "");
                YamlConfiguration playerData = plugin.getPlayerDataManager().getPlayerData(uuid);
                String birthdayString = playerData.getString("birthday");

                if (birthdayString != null && birthdayString.equals(todayString)) {
                    String playerName = playerData.getString("name");
                    if (playerName != null) {
                        birthdayPlayers.add(playerName);
                    }
                }
            }
        }

        if (!birthdayPlayers.isEmpty()) {
            handleBirthdayWish(sender, birthdayPlayers, todayString);
        } else {
            sender.sendMessage(Component.text("今天没有人过生日哦！")
                    .color(NamedTextColor.RED));
        }
    }

    private void handleBirthdayWish(Player sender, List<String> birthdayPlayers, String todayString) {
        String uuid = sender.getUniqueId().toString();
        YamlConfiguration wishData = plugin.getPlayerDataManager().getPlayerData(uuid);
        String wishKey = "wishes." + todayString;
        List<String> todayWishes = wishData.getStringList(wishKey);

        if (!todayWishes.contains(uuid)) {
            plugin.getServer().getRegionScheduler().execute(plugin, sender.getLocation(), () -> {
                ItemStack cake = new ItemStack(Material.CAKE, 1);
                sender.getInventory().addItem(cake);

                String playersString = String.join("、", birthdayPlayers);
                sender.sendMessage(Component.text("今天过生日的玩家：")
                        .color(NamedTextColor.GOLD)
                        .append(Component.text(playersString)
                                .color(NamedTextColor.YELLOW)));
                sender.sendMessage(Component.text("感谢你送上生日祝福！")
                        .color(NamedTextColor.GREEN));
            });

            todayWishes.add(uuid);
            wishData.set(wishKey, todayWishes);
            plugin.getPlayerDataManager().savePlayerData(uuid, wishData);
        } else {
            String message = plugin.getConfig().getString("messages.wish-already", "你今天已经送过生日祝福了！");
            sender.sendMessage(Component.text(message != null ? message : "你今天已经送过生日祝福了！")
                    .color(NamedTextColor.YELLOW));
        }
    }
}
