package com.zpedroo.market.objects;

import com.zpedroo.multieconomy.objects.general.Currency;
import org.bukkit.inventory.ItemStack;

import java.math.BigInteger;
import java.util.UUID;

public class MarketItem {

    private final long id;
    private final UUID sellerUniqueId;
    private final ItemStack item;
    private Currency currency;
    private BigInteger price;
    private final long expirationDateInMillis;
    private boolean update;

    public MarketItem(long id, UUID sellerUniqueId, ItemStack item, Currency currency, BigInteger price, long expirationDateInMillis) {
        this.id = id;
        this.sellerUniqueId = sellerUniqueId;
        this.item = item;
        this.currency = currency;
        this.price = price;
        this.expirationDateInMillis = expirationDateInMillis;
        this.update = false;
    }

    public long getId() {
        return id;
    }

    public UUID getSellerUniqueId() {
        return sellerUniqueId;
    }

    public ItemStack getItem() {
        return item.clone();
    }

    public Currency getCurrency() {
        return currency;
    }

    public BigInteger getPrice() {
        return price;
    }

    public long getExpirationDateInMillis() {
        return expirationDateInMillis;
    }

    public boolean isQueueUpdate() {
        return update;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public void setPrice(BigInteger price) {
        this.price = price;
    }

    public void setUpdate(boolean update) {
        this.update = update;
    }
}