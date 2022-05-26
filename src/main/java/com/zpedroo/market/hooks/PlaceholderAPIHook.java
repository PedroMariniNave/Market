package com.zpedroo.market.hooks;

import com.zpedroo.market.managers.DataManager;
import com.zpedroo.multieconomy.utils.formatter.NumberFormatter;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class PlaceholderAPIHook extends PlaceholderExpansion {

    private final Plugin plugin;

    public PlaceholderAPIHook(Plugin plugin) {
        this.plugin = plugin;
        this.register();
    }

    @NotNull
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @NotNull
    public String getIdentifier() {
        return "market";
    }

    @NotNull
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    public String onPlaceholderRequest(Player player, String identifier) {
        switch (identifier.toUpperCase()) {
            case "ITEMS_AMOUNT":
                return NumberFormatter.getInstance().formatDecimal(DataManager.getInstance().getSellingItemsAmount());
            default:
                return null;
        }
    }
}