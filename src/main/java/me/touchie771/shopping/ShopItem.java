package me.touchie771.shopping;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record ShopItem(ItemStack itemStack, int price, UUID owner) {

    public ShopItem {
        if (itemStack == null || itemStack.getType().isAir()) {
            throw new IllegalArgumentException("ItemStack cannot be null or air");
        }
        if (price < 0) {
            throw new IllegalArgumentException("Price cannot be negative");
        }
        if (owner == null) {
            throw new IllegalArgumentException("Owner cannot be null");
        }
    }

    public ItemStack getClonedItem() {
        return itemStack.clone();
    }

    public ItemStack getDisplayItem() {
        ItemStack display = itemStack.clone();
        ItemMeta meta = display.getItemMeta();
        
        if (meta != null) {
            List<Component> lore = meta.lore() != null ? new ArrayList<>(meta.lore()) : new ArrayList<>();
            
            lore.add(Component.empty());
            lore.add(Component.text("Price: ", NamedTextColor.GRAY)
                    .append(Component.text("$" + price, NamedTextColor.GREEN)));
            
            OfflinePlayer ownerPlayer = Bukkit.getOfflinePlayer(owner);
            String ownerName = ownerPlayer.getName() != null ? ownerPlayer.getName() : "Unknown";
            lore.add(Component.text("Seller: ", NamedTextColor.GRAY)
                    .append(Component.text(ownerName, NamedTextColor.YELLOW)));
            
            lore.add(Component.empty());
            lore.add(Component.text("Click to purchase!", NamedTextColor.LIGHT_PURPLE));
            
            meta.lore(lore);
            display.setItemMeta(meta);
        }
        
        return display;
    }

    public boolean isOwnedBy(UUID playerId) {
        return owner.equals(playerId);
    }
}