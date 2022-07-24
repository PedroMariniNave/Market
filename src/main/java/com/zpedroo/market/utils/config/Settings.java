package com.zpedroo.market.utils.config;

import com.zpedroo.market.utils.FileUtils;

import java.math.BigInteger;
import java.util.List;

public class Settings {

    public static final String COMMAND = FileUtils.get().getString(FileUtils.Files.CONFIG, "Settings.command");

    public static final List<String> ALIASES = FileUtils.get().getStringList(FileUtils.Files.CONFIG, "Settings.aliases");

    public static final String DATE_FORMAT = FileUtils.get().getString(FileUtils.Files.CONFIG, "Settings.date-format");

    public static final long SAVE_INTERVAL = FileUtils.get().getLong(FileUtils.Files.CONFIG, "Settings.save-interval");

    public static final long CHECK_INTERVAL = FileUtils.get().getLong(FileUtils.Files.CONFIG, "Settings.check-interval");

    public static final long SALE_DURATION = FileUtils.get().getLong(FileUtils.Files.CONFIG, "Settings.sale-duration");

    public static final BigInteger MAX_PRICE = NumberFormatter.getInstance().filter(FileUtils.get().getString(FileUtils.Files.CONFIG, "Settings.max-price"));
}