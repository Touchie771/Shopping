package me.touchie771.shopping;

import org.bukkit.inventory.ItemStack;

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

    public boolean isOwnedBy(UUID playerId) {
        return owner.equals(playerId);
    }
}