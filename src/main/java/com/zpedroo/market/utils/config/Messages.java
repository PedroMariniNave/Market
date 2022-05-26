package com.zpedroo.market.utils.config;

import com.zpedroo.market.utils.FileUtils;
import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class Messages {

    public static final String NEW_SALE = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.new-sale"));

    public static final String BOUGHT_ITEM_EVERYONE = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.bought-item-everyone"));

    public static final String BOUGHT_ITEM_PLAYER = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.bought-item-player"));

    public static final String NEED_ITEM_IN_HAND = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.need-item-in-hand"));

    public static final String INVALID_SALE = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.invalid-sale"));

    public static final String INVALID_AMOUNT = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.invalid-amount"));

    public static final String INSUFFICIENT_CURRENCY = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.insufficient-currency"));

    public static final String ITEM_NOT_FOUND = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.item-not-found"));

    public static final String EXPIRED_ITEM = getColored(FileUtils.get().getString(FileUtils.Files.CONFIG, "Messages.expired-item"));

    public static final List<String> CHOOSE_PRICE = getColored(FileUtils.get().getStringList(FileUtils.Files.CONFIG, "Messages.choose-price"));

    private static List<String> getColored(List<String> strList) {
        List<String> coloredList = new ArrayList<>(strList.size());
        for (String str : strList) {
            coloredList.add(getColored(str));
        }

        return coloredList;
    }

    private static String getColored(String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }
}