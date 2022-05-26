package com.zpedroo.market.listeners;

import com.zpedroo.market.Market;
import com.zpedroo.market.managers.MarketManager;
import com.zpedroo.market.objects.PreMarketItem;
import com.zpedroo.market.utils.config.Messages;
import com.zpedroo.market.utils.config.NumberFormatter;
import com.zpedroo.market.utils.menu.Menus;
import com.zpedroo.multieconomy.objects.general.Currency;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.inventory.ItemStack;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class PlayerChatListener implements Listener {

    private static final Map<Player, PreMarketItem> playersSettingPrice = new HashMap<>(2);

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onSettingPriceChat(AsyncPlayerChatEvent event) {
        if (!playersSettingPrice.containsKey(event.getPlayer())) return;

        event.setCancelled(true);

        Player player = event.getPlayer();
        PreMarketItem preMarketItem = playersSettingPrice.remove(player);
        BigInteger price = NumberFormatter.getInstance().filter(event.getMessage());
        if (price.signum() < 0) {
            player.sendMessage(Messages.INVALID_AMOUNT);
            return;
        }

        ItemStack auctionItem = preMarketItem.getItem();
        Currency currency = preMarketItem.getCurrency();
        long durationInMillis = preMarketItem.getDurationInMillis();

        if (player.getInventory().first(auctionItem) == -1) {
            player.sendMessage(Messages.ITEM_NOT_FOUND);
            return;
        }

        player.getInventory().removeItem(auctionItem);
        MarketManager.getInstance().createSaleAndCache(player, auctionItem, currency, price, durationInMillis + System.currentTimeMillis());
        Market.get().getServer().getScheduler().runTaskLater(Market.get(), () -> Menus.getInstance().openPlayerSalesMenu(player), 0L);

        Bukkit.broadcastMessage(StringUtils.replaceEach(Messages.NEW_SALE, new String[]{
                "{player}",
                "{price}",
                "{item}"
        }, new String[]{
                player.getName(),
                currency.getAmountDisplay(price),
                auctionItem.hasItemMeta() ? auctionItem.getItemMeta().hasDisplayName() ? auctionItem.getItemMeta().getDisplayName()
                        : auctionItem.getType().toString() : auctionItem.getType().toString()
        }));
    }

    public static Map<Player, PreMarketItem> getPlayersSettingPrice() {
        return playersSettingPrice;
    }
}