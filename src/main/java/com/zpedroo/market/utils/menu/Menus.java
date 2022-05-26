package com.zpedroo.market.utils.menu;

import com.zpedroo.market.utils.config.NumberFormatter;
import com.zpedroo.multieconomy.api.CurrencyAPI;
import com.zpedroo.multieconomy.objects.general.Currency;
import com.zpedroo.market.listeners.PlayerChatListener;
import com.zpedroo.market.managers.MarketManager;
import com.zpedroo.market.managers.DataManager;
import com.zpedroo.market.objects.MarketItem;
import com.zpedroo.market.objects.PlayerData;
import com.zpedroo.market.objects.PreMarketItem;
import com.zpedroo.market.utils.FileUtils;
import com.zpedroo.market.utils.builder.InventoryBuilder;
import com.zpedroo.market.utils.builder.InventoryUtils;
import com.zpedroo.market.utils.builder.ItemBuilder;
import com.zpedroo.market.utils.config.Messages;
import com.zpedroo.market.utils.config.TimeFormatter;
import org.apache.commons.lang3.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public class Menus extends InventoryUtils {

    private static Menus instance;
    public static Menus getInstance() { return instance; }

    private final ItemStack nextPageItem = ItemBuilder.build(FileUtils.get().getFile(FileUtils.Files.CONFIG).get(), "Next-Page").build();;
    private final ItemStack previousPageItem = ItemBuilder.build(FileUtils.get().getFile(FileUtils.Files.CONFIG).get(), "Previous-Page").build();;

    public Menus() {
        instance = this;
    }

    public void openMainMenu(Player player) {
        FileUtils.Files file = FileUtils.Files.MAIN;

        String title = ChatColor.translateAlternateColorCodes('&', FileUtils.get().getString(file, "Inventory.title"));
        int size = FileUtils.get().getInt(file, "Inventory.size");

        InventoryBuilder inventory = new InventoryBuilder(title, size);

        for (String items : FileUtils.get().getSection(file, "Inventory.items")) {
            ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Inventory.items." + items, new String[]{
                    "{player}"
            }, new String[]{
                    player.getName()
            }).build();
            int slot = FileUtils.get().getInt(file, "Inventory.items." + items + ".slot");
            String action = FileUtils.get().getString(file, "Inventory.items." + items + ".action");

            inventory.addItem(item, slot, () -> {
                switch (action.toUpperCase()) {
                    case "NEW_SALE":
                        if (player.getItemInHand() == null || player.getItemInHand().getType().equals(Material.AIR)) {
                            player.closeInventory();
                            player.sendMessage(Messages.NEED_ITEM_IN_HAND);
                            return;
                        }

                        ItemStack itemInHand = player.getItemInHand().clone();
                        openItemConfirmationMenu(player, itemInHand);
                        break;
                    case "ACTIVE_SALES":
                        openActiveSalesMenu(player);
                        break;
                    case "PLAYER_SALES":
                        openPlayerSalesMenu(player);
                        break;
                    case "PLAYER_ITEMS":
                        openPlayerItemsMenu(player);
                        break;
                }
            }, ActionType.ALL_CLICKS);
        }

        inventory.open(player);
    }

    public void openActiveSalesMenu(Player player) {
        FileUtils.Files file = FileUtils.Files.ACTIVE_SALES;

        String title = ChatColor.translateAlternateColorCodes('&', FileUtils.get().getString(file, "Inventory.title"));
        int size = FileUtils.get().getInt(file, "Inventory.size");

        int nextPageSlot = FileUtils.get().getInt(file, "Inventory.next-page-slot");
        int previousPageSlot = FileUtils.get().getInt(file, "Inventory.previous-page-slot");

        InventoryBuilder inventory = new InventoryBuilder(title, size, previousPageItem, previousPageSlot, nextPageItem, nextPageSlot);
        List<MarketItem> activeMarketItems = DataManager.getInstance().getCache().getActiveSales();

        if (!activeMarketItems.isEmpty()) {
            List<String> itemLore = FileUtils.get().getStringList(file, "Item-Lore");
            String[] slots = FileUtils.get().getString(file, "Inventory.slots").replace(" ", "").split(",");
            int i = -1;
            for (MarketItem marketItem : activeMarketItems) {
                if (StringUtils.equals(marketItem.getSellerUniqueId().toString(), player.getUniqueId().toString())) continue;
                if (++i >= slots.length) i = 0;

                ItemStack item = marketItem.getItem();
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>(4);

                    for (String toAdd : itemLore) {
                        lore.add(StringUtils.replaceEach(ChatColor.translateAlternateColorCodes('&', toAdd), new String[]{
                                "{seller}",
                                "{price}",
                                "{expiration}"
                        }, new String[]{
                                Bukkit.getOfflinePlayer(marketItem.getSellerUniqueId()).getName(),
                                marketItem.getCurrency().getAmountDisplay(marketItem.getPrice()),
                                TimeFormatter.millisToFormattedTime(marketItem.getExpirationDateInMillis() - System.currentTimeMillis())
                        }));
                    }

                    meta.setLore(lore);
                    item.setItemMeta(meta);
                }

                int slot = Integer.parseInt(slots[i]);

                inventory.addItem(item, slot, () -> {
                    if (!MarketManager.getInstance().isActiveItem(marketItem)) {
                        player.sendMessage(Messages.INVALID_SALE);
                        openActiveSalesMenu(player);
                        return;
                    }

                    BigInteger playerCurrencyAmount = CurrencyAPI.getCurrencyAmount(player.getUniqueId(), marketItem.getCurrency());
                    BigInteger price = marketItem.getPrice();
                    if (playerCurrencyAmount.compareTo(price) < 0) {
                        player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1f, 1f);
                        player.sendMessage(StringUtils.replaceEach(Messages.INSUFFICIENT_CURRENCY, new String[]{
                                "{has}",
                                "{need}"
                        }, new String[]{
                                NumberFormatter.getInstance().format(playerCurrencyAmount),
                                NumberFormatter.getInstance().format(price)
                        }));
                        return;
                    }

                    inventory.close(player);
                    MarketManager.getInstance().buyItem(player, marketItem);
                }, ActionType.ALL_CLICKS);
            }
        } else {
            ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Empty").build();
            int slot = FileUtils.get().getInt(file, "Empty.slot");

            inventory.addItem(item, slot);
        }

        inventory.open(player);
    }

    public void openPlayerSalesMenu(Player player) {
        FileUtils.Files file = FileUtils.Files.PLAYER_SALES;

        String title = ChatColor.translateAlternateColorCodes('&', FileUtils.get().getString(file, "Inventory.title"));
        int size = FileUtils.get().getInt(file, "Inventory.size");

        int nextPageSlot = FileUtils.get().getInt(file, "Inventory.next-page-slot");
        int previousPageSlot = FileUtils.get().getInt(file, "Inventory.previous-page-slot");

        InventoryBuilder inventory = new InventoryBuilder(title, size, previousPageItem, previousPageSlot, nextPageItem, nextPageSlot);
        List<MarketItem> playerMarketItems = DataManager.getInstance().getCache().getPlayerActiveSales(player);

        if (playerMarketItems != null && !playerMarketItems.isEmpty()) {
            List<String> itemLore = FileUtils.get().getStringList(file, "Item-Lore");
            String[] slots = FileUtils.get().getString(file, "Inventory.slots").replace(" ", "").split(",");
            int i = -1;
            for (MarketItem marketItem : playerMarketItems) {
                if (++i >= slots.length) i = 0;

                ItemStack item = marketItem.getItem();
                ItemMeta meta = item.getItemMeta();
                if (meta != null) {
                    List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>(4);

                    for (String toAdd : itemLore) {
                        lore.add(StringUtils.replaceEach(ChatColor.translateAlternateColorCodes('&', toAdd), new String[]{
                                "{price}",
                                "{expiration}"
                        }, new String[]{
                                marketItem.getCurrency().getAmountDisplay(marketItem.getPrice()),
                                TimeFormatter.millisToFormattedTime(marketItem.getExpirationDateInMillis() - System.currentTimeMillis())
                        }));
                    }

                    meta.setLore(lore);
                    item.setItemMeta(meta);
                }

                int slot = Integer.parseInt(slots[i]);

                inventory.addItem(item, slot, () -> {
                    if (!MarketManager.getInstance().isActiveItem(marketItem)) {
                        player.sendMessage(Messages.INVALID_SALE);
                        return;
                    }

                    MarketManager.getInstance().cancelSaleAndRefoundItem(player, marketItem);
                    openPlayerSalesMenu(player);
                }, ActionType.ALL_CLICKS);
            }
        } else {
            ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Empty").build();
            int slot = FileUtils.get().getInt(file, "Empty.slot");

            inventory.addItem(item, slot);
        }

        inventory.open(player);
    }

    public void openPlayerItemsMenu(Player player) {
        FileUtils.Files file = FileUtils.Files.PLAYER_ITEMS;

        String title = ChatColor.translateAlternateColorCodes('&', FileUtils.get().getString(file, "Inventory.title"));
        int size = FileUtils.get().getInt(file, "Inventory.size");

        int nextPageSlot = FileUtils.get().getInt(file, "Inventory.next-page-slot");
        int previousPageSlot = FileUtils.get().getInt(file, "Inventory.previous-page-slot");

        InventoryBuilder inventory = new InventoryBuilder(title, size, previousPageItem, previousPageSlot, nextPageItem, nextPageSlot);

        PlayerData data = DataManager.getInstance().getPlayerData(player);
        List<ItemStack> playerItems = data.getItemsToCollect();

        if (!playerItems.isEmpty()) {
            List<String> itemLore = FileUtils.get().getStringList(file, "Item-Lore");
            String[] slots = FileUtils.get().getString(file, "Inventory.slots").replace(" ", "").split(",");
            int i = -1;
            for (ItemStack item : playerItems) {
                if (++i >= slots.length) i = 0;

                ItemStack itemClone = item.clone();
                ItemMeta meta = itemClone.getItemMeta();
                if (meta != null) {
                    List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>(4);

                    for (String toAdd : itemLore) {
                        lore.add(ChatColor.translateAlternateColorCodes('&', toAdd));
                    }

                    meta.setLore(lore);
                    itemClone.setItemMeta(meta);
                }

                int slot = Integer.parseInt(slots[i]);

                inventory.addItem(itemClone, slot, () -> {
                    data.removeItemToCollect(item);

                    if (player.getInventory().firstEmpty() != -1) {
                        player.getInventory().addItem(item);
                    } else {
                        player.getWorld().dropItemNaturally(player.getLocation(), item);
                    }

                    openPlayerItemsMenu(player);
                }, ActionType.ALL_CLICKS);
            }
        } else {
            ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Empty").build();
            int slot = FileUtils.get().getInt(file, "Empty.slot");

            inventory.addItem(item, slot);
        }

        inventory.open(player);
    }

    public void openItemConfirmationMenu(Player player, ItemStack itemToConfirm) {
        FileUtils.Files file = FileUtils.Files.ITEM_CONFIRMATION;

        String title = ChatColor.translateAlternateColorCodes('&', FileUtils.get().getString(file, "Inventory.title"));
        int size = FileUtils.get().getInt(file, "Inventory.size");

        InventoryBuilder inventory = new InventoryBuilder(title, size);

        for (String items : FileUtils.get().getSection(file, "Inventory.items")) {
            ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Inventory.items." + items).build();
            int slot = FileUtils.get().getInt(file, "Inventory.items." + items + ".slot");
            String action = FileUtils.get().getString(file, "Inventory.items." + items + ".action");

            inventory.addItem(item, slot, () -> {
                switch (action.toUpperCase()) {
                    case "CONFIRM":
                        openSelectCurrencyMenu(player, new PreMarketItem(itemToConfirm));
                        break;
                    case "CANCEL":
                        player.closeInventory();
                        break;
                }
            }, ActionType.ALL_CLICKS);
        }

        int itemToConfirmSlot = FileUtils.get().getInt(file, "Inventory.item-slot");
        inventory.addItem(itemToConfirm, itemToConfirmSlot);

        inventory.open(player);
    }

    public void openSelectCurrencyMenu(Player player, PreMarketItem preMarketItem) {
        FileUtils.Files file = FileUtils.Files.SELECT_CURRENCY;

        String title = ChatColor.translateAlternateColorCodes('&', FileUtils.get().getString(file, "Inventory.title"));
        int size = FileUtils.get().getInt(file, "Inventory.size");

        InventoryBuilder inventory = new InventoryBuilder(title, size);

        ItemStack auctionItem = preMarketItem.getItem();
        long durationInMillis = preMarketItem.getDurationInMillis();

        for (String str : FileUtils.get().getSection(file, "Inventory.items")) {
            ItemStack item = ItemBuilder.build(FileUtils.get().getFile(file).get(), "Inventory.items." + str).build();
            int slot = FileUtils.get().getInt(file, "Inventory.items." + str + ".slot");

            String currencyName = FileUtils.get().getString(file, "Inventory.items." + str + ".currency");
            Currency currency = CurrencyAPI.getCurrency(currencyName);
            inventory.addItem(item, slot, () -> {
                if (currency == null) return;

                preMarketItem.setCurrency(currency);
                inventory.close(player);

                PlayerChatListener.getPlayersSettingPrice().put(player, preMarketItem);

                for (int x = 0; x < 25; ++x) {
                    player.sendMessage("");
                }

                for (String msg : Messages.CHOOSE_PRICE) {
                    player.sendMessage(StringUtils.replaceEach(msg, new String[]{
                            "{item}",
                            "{currency}",
                            "{expiration}"
                    }, new String[]{
                            auctionItem.hasItemMeta() ? auctionItem.getItemMeta().hasDisplayName() ? auctionItem.getItemMeta().getDisplayName() :
                                    auctionItem.getType().toString() : auctionItem.getType().toString(),
                            currency.getCurrencyDisplay(),
                            TimeFormatter.millisToFormattedTime(durationInMillis)
                    }));
                }
            }, ActionType.ALL_CLICKS);
        }

        inventory.open(player);
    }
}