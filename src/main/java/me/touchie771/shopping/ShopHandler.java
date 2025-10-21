package me.touchie771.shopping;

import java.util.ArrayList;
import java.util.List;

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
}