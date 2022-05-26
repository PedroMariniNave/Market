package com.zpedroo.market.objects;

import com.zpedroo.market.utils.config.Settings;
import com.zpedroo.multieconomy.objects.general.Currency;
import org.bukkit.inventory.ItemStack;

import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

public class PreMarketItem {

    private final ItemStack item;
    private Currency currency;
    private BigInteger price;
    private final long durationInMillis = TimeUnit.SECONDS.toMillis(Settings.SALE_DURATION);

    public PreMarketItem(ItemStack item) {
        this.item = item;
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

    public long getDurationInMillis() {
        return durationInMillis;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public void setPrice(BigInteger price) {
        this.price = price;
    }
}