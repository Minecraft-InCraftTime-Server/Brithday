package ict.minesunshineone.birthday;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class BirthdayPlaceholder extends PlaceholderExpansion {

    private final BirthdayPlugin plugin;

    public BirthdayPlaceholder(BirthdayPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @NotNull
    String getIdentifier() {
        return "birthday";
    }

    @Override
    public @NotNull
    String getAuthor() {
        return "minesunshineone";
    }

    @Override
    public @NotNull
    String getVersion() {
        return "1.0";
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) {
            return "";
        }

        if (identifier.equals("date")) {
            String birthday = plugin.getPlayerDataManager().getBirthday(player.getUniqueId().toString());
            if (birthday != null) {
                String[] parts = birthday.split("-");
                return parts[0] + "月" + parts[1] + "日";
            }
            return "未设置";
        }

        return null;
    }
}
