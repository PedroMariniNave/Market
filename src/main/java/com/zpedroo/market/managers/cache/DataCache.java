package com.zpedroo.market.managers.cache;

import com.zpedroo.market.objects.PlayerData;
import com.zpedroo.market.Market;
import com.zpedroo.market.mysql.DBConnection;
import com.zpedroo.market.objects.MarketItem;
import org.apache.commons.lang.StringUtils;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class DataCache {

    private Map<Long, MarketItem> sales;
    private final List<Long> salesToDelete;
    private final Map<Player, PlayerData> playerData;

    public DataCache() {
        Market.get().getServer().getScheduler().runTaskLater(Market.get(), () -> {
            this.sales = DBConnection.getInstance().getDBManager().getAuctionsFromDatabase();
        }, 40L);
        this.salesToDelete = new ArrayList<>(16);
        this.playerData = new HashMap<>(32);
    }

    public Map<Long, MarketItem> getSales() {
        return sales;
    }

    public List<Long> getSalesIdsToDelete() {
        return salesToDelete;
    }

    public Map<Player, PlayerData> getPlayerData() {
        return playerData;
    }

    public List<MarketItem> getActiveSales() {
        return new HashSet<>(
                sales.values()).stream().filter(sale -> System.currentTimeMillis() < sale.getExpirationDateInMillis()).collect(Collectors.toList()
        );
    }

    public List<MarketItem> getPlayerActiveSales(Player player) {
        return getActiveSales().stream().filter(
                sale -> StringUtils.equals(player.getUniqueId().toString(), sale.getSellerUniqueId().toString())).collect(Collectors.toList()
        );
    }
}