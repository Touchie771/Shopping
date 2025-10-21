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

    public static Inventory constructMenu() {
        Inventory shopMenu = Bukkit.createInventory(null, 45, Component.text("Shop Menu",
                NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD));

        // Construct the menu by sorting each shop item in terms of price
        items.sort(Comparator.comparingDouble(ShopItem::price));

        // Add each item to the menu
        for (int i = 0; i < items.size(); i++) {
            ShopItem item = items.get(i);
            shopMenu.setItem(i, item.getDisplayItem());
        }

        // Add filler item
        addFillerItem(shopMenu);

        return shopMenu;
    }

    public static Inventory constructRemovalMenu(java.util.UUID playerId) {
        Inventory removalMenu = Bukkit.createInventory(null, 45, Component.text("Your Shop Items",
                NamedTextColor.RED, TextDecoration.BOLD));

        List<ShopItem> playerItems = items.stream()
                .filter(item -> item.isOwnedBy(playerId))
                .sorted(Comparator.comparingDouble(ShopItem::price))
                .toList();

        for (int i = 0; i < playerItems.size() && i < 45; i++) {
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
            
            removalMenu.setItem(i, displayItem);
        }

        addFillerItem(removalMenu);
        return removalMenu;
    }

    public static ShopItem getPlayerItemAtSlot(java.util.UUID playerId, int slot) {
        List<ShopItem> playerItems = items.stream()
                .filter(item -> item.isOwnedBy(playerId))
                .sorted(Comparator.comparingDouble(ShopItem::price))
                .toList();
        
        if (slot < 0 || slot >= playerItems.size()) {
            return null;
        }
        return playerItems.get(slot);
    }

    public static void addFillerItem(Inventory inventory) {
        ItemStack fillerItem = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1);
        ItemMeta fillerItemMeta = fillerItem.getItemMeta();
        fillerItemMeta.displayName(Component.empty());
        fillerItem.setItemMeta(fillerItemMeta);

        for (int i = 0; i < inventory.getSize(); i++) {
            if (inventory.getItem(i) == null) {
                inventory.setItem(i, fillerItem);
            }
        }
    }
}