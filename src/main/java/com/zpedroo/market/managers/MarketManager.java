package com.zpedroo.market.managers;

import com.zpedroo.market.objects.MarketItem;
import com.zpedroo.market.objects.PlayerData;
import com.zpedroo.market.utils.api.OfflinePlayerAPI;
import com.zpedroo.market.utils.config.Messages;
import com.zpedroo.multieconomy.api.CurrencyAPI;
import com.zpedroo.multieconomy.objects.general.Currency;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigInteger;
import java.util.HashSet;
import java.util.UUID;

public class MarketManager extends DataManager {

    private static MarketManager instance;
    public static MarketManager getInstance() { return instance; }

    public MarketManager() {
        instance = this;
    }

    public void checkAllItemsExpiration() {
        new HashSet<>(getCache().getSales().values()).stream().filter(
                sale -> System.currentTimeMillis() >= sale.getExpirationDateInMillis()
        ).forEach(finishedSale -> {
            getCache().getSales().remove(finishedSale.getId());
            getCache().getSalesIdsToDelete().add(finishedSale.getId());

            OfflinePlayer sellerOfflinePlayer = Bukkit.getOfflinePlayer(finishedSale.getSellerUniqueId());
            Player seller = OfflinePlayerAPI.getPlayer(sellerOfflinePlayer.getName());
            if (seller == null) return;

            PlayerData data = getPlayerData(seller);
            if (data == null) return;

            ItemStack item = finishedSale.getItem();
            data.addItemToCollect(item);

            if (seller.isOnline()) {
                seller.sendMessage(StringUtils.replaceEach(Messages.EXPIRED_ITEM, new String[]{
                        "{item}",
                        "{price}"
                }, new String[]{
                        item.hasItemMeta() ? item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : item.getType().toString() : item.getType().toString(),
                        finishedSale.getCurrency().getAmountDisplay(finishedSale.getPrice())
                }));
            }
        });
    }

    public void cancelSaleAndRefoundItem(Player player, MarketItem marketItem) {
        if (!isActiveItem(marketItem)) return;

        getCache().getSales().remove(marketItem.getId());

        PlayerData data = getPlayerData(player);
        if (data == null) return;

        data.addItemToCollect(marketItem.getItem());
    }

    public boolean isActiveItem(MarketItem marketItem) {
        return getCache().getActiveSales().contains(marketItem);
    }

    public void buyItem(Player player, MarketItem marketItem) {
        Currency currency = marketItem.getCurrency();
        BigInteger price = marketItem.getPrice();
        BigInteger playerCurrencyAmount = CurrencyAPI.getCurrencyAmount(player.getUniqueId(), currency);
        if (playerCurrencyAmount.compareTo(price) < 0) return;

        PlayerData data = getPlayerData(player);
        if (data == null) return;

        getCache().getSales().remove(marketItem.getId());
        getCache().getSalesIdsToDelete().add(marketItem.getId());

        ItemStack item = marketItem.getItem();
        data.addItemToCollect(item);

        UUID sellerUUID = marketItem.getSellerUniqueId();
        CurrencyAPI.removeCurrencyAmount(player.getUniqueId(), currency, price);
        CurrencyAPI.addCurrencyAmount(sellerUUID, currency, price);

        String[] placeholders = new String[]{
                "{seller}",
                "{buyer}",
                "{item}",
                "{price}"
        };
        String[] replacers = new String[]{
                Bukkit.getOfflinePlayer(sellerUUID).getName(),
                player.getName(),
                item.hasItemMeta() ? item.getItemMeta().hasDisplayName() ? item.getItemMeta().getDisplayName() : item.getType().toString() : item.getType().toString(),
                currency.getAmountDisplay(price)
        };

        Bukkit.getOnlinePlayers().stream().filter(players -> players.getUniqueId() != player.getUniqueId()).forEach(target -> {
            target.sendMessage(StringUtils.replaceEach(Messages.BOUGHT_ITEM_EVERYONE, placeholders, replacers));
        });
        player.sendMessage(StringUtils.replaceEach(Messages.BOUGHT_ITEM_PLAYER, placeholders, replacers));
        player.playSound(player.getLocation(), Sound.ORB_PICKUP, 0.5f, 10f);
    }

    public void createSaleAndCache(Player player, ItemStack item, Currency currency, BigInteger price, long expirationDateInMillis) {
        long saleId = System.nanoTime();
        MarketItem marketItem = new MarketItem(saleId, player.getUniqueId(), item, currency, price, expirationDateInMillis);
        marketItem.setUpdate(true);

        getCache().getSales().put(saleId, marketItem);
    }
}