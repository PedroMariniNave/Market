package com.zpedroo.market.utils.color;

import org.bukkit.ChatColor;

import java.util.ArrayList;
import java.util.List;

public class Colorize {

    public static List<String> getColored(List<String> strList) {
        List<String> coloredList = new ArrayList<>(strList.size());
        for (String str : strList) {
            coloredList.add(getColored(str));
        }

        return coloredList;
    }

    public static String getColored(String str) {
        return ChatColor.translateAlternateColorCodes('&', str);
    }
}