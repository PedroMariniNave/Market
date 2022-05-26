package com.zpedroo.market.tasks;

import com.zpedroo.market.managers.MarketManager;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

import static com.zpedroo.market.utils.config.Settings.CHECK_INTERVAL;

public class CheckTask extends BukkitRunnable {

    public CheckTask(Plugin plugin) {
        this.runTaskTimerAsynchronously(plugin, 20 * CHECK_INTERVAL, 20 * CHECK_INTERVAL);
    }

    @Override
    public void run() {
        MarketManager.getInstance().checkAllItemsExpiration();
    }
}