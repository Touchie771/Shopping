package me.touchie771.shopping;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public record ShopHandler() {

    private static final List<ShopItem> items = new ArrayList<>();
    private static final int ITEMS_PER_PAGE = 36;

    public static List<ShopItem> getItems() {
        return items;
    }

    public static void addItem(ShopItem item) {
        items.add(item);
    }

    public static void removeItem(ShopItem item) {
        items.remove(item);
    }

    public static void clearItems() {
        items.clear();
    }

    public static Inventory constructMenu(int page) {
        items.sort(Comparator.comparingDouble(ShopItem::price));
        
        int totalPages = (int) Math.ceil((double) items.size() / ITEMS_PER_PAGE);
        if (totalPages == 0) totalPages = 1;
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;
        
        Inventory shopMenu = Bukkit.createInventory(null, 45, 
                Component.text("Shop Menu - Page " + page + "/" + totalPages,
                        NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD));

        int startIndex = (page - 1) * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, items.size());

        for (int i = startIndex; i < endIndex; i++) {
            ShopItem item = items.get(i);
            shopMenu.setItem(i - startIndex, item.getDisplayItem());
        }

        addNavigationButtons(shopMenu, page, totalPages);
        addFillerItem(shopMenu);

        return shopMenu;
    }

    public static Inventory constructMenu() {
        return constructMenu(1);
    }

    public static Inventory constructRemovalMenu(java.util.UUID playerId, int page) {
        List<ShopItem> playerItems = items.stream()
                .filter(item -> item.isOwnedBy(playerId))
                .sorted(Comparator.comparingDouble(ShopItem::price))
                .toList();

        int totalPages = (int) Math.ceil((double) playerItems.size() / ITEMS_PER_PAGE);
        if (totalPages == 0) totalPages = 1;
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        Inventory removalMenu = Bukkit.createInventory(null, 45, 
                Component.text("Your Shop Items - Page " + page + "/" + totalPages,
                        NamedTextColor.RED, TextDecoration.BOLD));

        int startIndex = (page - 1) * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, playerItems.size());

        for (int i = startIndex; i < endIndex; i++) {
            ShopItem item = playerItems.get(i);
            ItemStack displayItem = item.getDisplayItem();
            ItemMeta meta = displayItem.getItemMeta();
            
            if (meta != null) {
                List<Component> lore = meta.lore() != null ? new ArrayList<>(Objects.requireNonNull(meta.lore())) : new ArrayList<>();
                lore.add(Component.empty());
                lore.add(Component.text("Click to remove from shop!", NamedTextColor.RED));
                meta.lore(lore);
                displayItem.setItemMeta(meta);
            }
            
            removalMenu.setItem(i - startIndex, displayItem);
        }

        addNavigationButtons(removalMenu, page, totalPages);
        addFillerItem(removalMenu);
        return removalMenu;
    }

    public static Inventory constructRemovalMenu(java.util.UUID playerId) {
        return constructRemovalMenu(playerId, 1);
    }

    public static ShopItem getPlayerItemAtSlot(java.util.UUID playerId, int slot, int page) {
        List<ShopItem> playerItems = items.stream()
                .filter(item -> item.isOwnedBy(playerId))
                .sorted(Comparator.comparingDouble(ShopItem::price))
                .toList();
        
        int actualIndex = (page - 1) * ITEMS_PER_PAGE + slot;
        if (actualIndex < 0 || actualIndex >= playerItems.size()) {
            return null;
        }
        return playerItems.get(actualIndex);
    }

    public static ShopItem getShopItemAtSlot(int slot, int page) {
        items.sort(Comparator.comparingDouble(ShopItem::price));
        int actualIndex = (page - 1) * ITEMS_PER_PAGE + slot;
        if (actualIndex < 0 || actualIndex >= items.size()) {
            return null;
        }
        return items.get(actualIndex);
    }

    private static void addNavigationButtons(Inventory inventory, int currentPage, int totalPages) {
        ItemStack prevButton;
        if (currentPage > 1) {
            prevButton = new ItemStack(Material.ARROW, 1);
            ItemMeta prevMeta = prevButton.getItemMeta();
            prevMeta.displayName(Component.text("Previous Page", NamedTextColor.YELLOW, TextDecoration.BOLD));
            prevMeta.lore(List.of(Component.text("Go to page " + (currentPage - 1), NamedTextColor.GRAY)));
            prevButton.setItemMeta(prevMeta);
        } else {
            prevButton = new ItemStack(Material.BARRIER, 1);
            ItemMeta prevMeta = prevButton.getItemMeta();
            prevMeta.displayName(Component.text("Previous Page", NamedTextColor.DARK_GRAY, TextDecoration.BOLD));
            prevMeta.lore(List.of(Component.text("No previous page", NamedTextColor.DARK_GRAY)));
            prevButton.setItemMeta(prevMeta);
        }
        inventory.setItem(36, prevButton);

        ItemStack nextButton;
        if (currentPage < totalPages) {
            nextButton = new ItemStack(Material.ARROW, 1);
            ItemMeta nextMeta = nextButton.getItemMeta();
            nextMeta.displayName(Component.text("Next Page", NamedTextColor.YELLOW, TextDecoration.BOLD));
            nextMeta.lore(List.of(Component.text("Go to page " + (currentPage + 1), NamedTextColor.GRAY)));
            nextButton.setItemMeta(nextMeta);
        } else {
            nextButton = new ItemStack(Material.BARRIER, 1);
            ItemMeta nextMeta = nextButton.getItemMeta();
            nextMeta.displayName(Component.text("Next Page", NamedTextColor.DARK_GRAY, TextDecoration.BOLD));
            nextMeta.lore(List.of(Component.text("No next page", NamedTextColor.DARK_GRAY)));
            nextButton.setItemMeta(nextMeta);
        }
        inventory.setItem(44, nextButton);
    }

    public static int getPageFromTitle(Component title) {
        String plainTitle = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(title);
        if (plainTitle.contains("Page ")) {
            try {
                String pageInfo = plainTitle.substring(plainTitle.indexOf("Page ") + 5);
                String pageNum = pageInfo.split("/")[0].trim();
                return Integer.parseInt(pageNum);
            } catch (Exception e) {
                return 1;
            }
        }
        return 1;
    }

    public static void addFillerItem(Inventory inventory) {
        ItemStack fillerItem = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1);
        ItemMeta fillerItemMeta = fillerItem.getItemMeta();
        fillerItemMeta.displayName(Component.empty());
        fillerItem.setItemMeta(fillerItemMeta);

        for (int i = 0; i < ITEMS_PER_PAGE; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, fillerItem);
            }
        }
        
        for (int i = 37; i < 44; i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, fillerItem);
            }
        }
    }
}