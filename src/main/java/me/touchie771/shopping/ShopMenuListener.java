package me.touchie771.shopping;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public record ShopMenuListener(Shopping plugin) implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        Component title = event.getView().title();
        boolean isShopMenu = title.equals(Component.text("Shop Menu", NamedTextColor.LIGHT_PURPLE, TextDecoration.BOLD));
        boolean isRemovalMenu = title.equals(Component.text("Your Shop Items", NamedTextColor.RED, TextDecoration.BOLD));

        if (!isShopMenu && !isRemovalMenu) {
            return;
        }

        event.setCancelled(true);
        
        if (event.getClickedInventory() == null || event.getClickedInventory().equals(player.getInventory())) {
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.GRAY_STAINED_GLASS_PANE) {
            return;
        }

        int slot = event.getSlot();

        if (isRemovalMenu) {
            handleRemovalMenuClick(player, slot, clickedItem);
            return;
        }

        ShopItem shopItem = getShopItemAtSlot(slot);

        if (shopItem == null) {
            return;
        }

        if (shopItem.isOwnedBy(player.getUniqueId())) {
            player.sendMessage(Component.text("You cannot buy your own item!", NamedTextColor.RED));
            return;
        }

        Economy economy = plugin.getEconomy();
        if (economy == null) {
            player.sendMessage(Component.text("Economy system is not available!", NamedTextColor.RED));
            return;
        }

        double balance = economy.getBalance(player);
        if (balance < shopItem.price()) {
            player.sendMessage(Component.text("You don't have enough money! Need $" + shopItem.price() +
                    " but only have $" + String.format("%.2f", balance), NamedTextColor.RED));
            return;
        }

        if (!player.getInventory().addItem(shopItem.getClonedItem()).isEmpty()) {
            player.sendMessage(Component.text("Your inventory is full!", NamedTextColor.RED));
            return;
        }

        economy.withdrawPlayer(player, shopItem.price());
        economy.depositPlayer(plugin.getServer().getOfflinePlayer(shopItem.owner()), shopItem.price());

        ShopHandler.removeItem(shopItem);
        plugin.getDataManager().saveItems();
        player.openInventory(ShopHandler.constructMenu());

        player.sendMessage(Component.text("Successfully purchased ", NamedTextColor.GREEN)
                .append(clickedItem.displayName())
                .append(Component.text(" for $" + shopItem.price(), NamedTextColor.GREEN)));
    }

    private void handleRemovalMenuClick(Player player, int slot, ItemStack clickedItem) {
        ShopItem shopItem = ShopHandler.getPlayerItemAtSlot(player.getUniqueId(), slot);

        if (shopItem == null) {
            return;
        }

        if (!player.getInventory().addItem(shopItem.getClonedItem()).isEmpty()) {
            player.sendMessage(Component.text("Your inventory is full!", NamedTextColor.RED));
            return;
        }

        ShopHandler.removeItem(shopItem);
        plugin.getDataManager().saveItems();
        player.openInventory(ShopHandler.constructRemovalMenu(player.getUniqueId()));

        player.sendMessage(Component.text("Successfully removed ", NamedTextColor.GREEN)
                .append(clickedItem.displayName())
                .append(Component.text(" from the shop", NamedTextColor.GREEN)));
    }

    private ShopItem getShopItemAtSlot(int slot) {
        if (slot < 0 || slot >= ShopHandler.getItems().size()) {
            return null;
        }
        return ShopHandler.getItems().get(slot);
    }
}