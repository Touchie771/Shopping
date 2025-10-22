package me.touchie771.shopping.shop;

import me.touchie771.shopping.Shopping;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
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
        String plainTitle = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(title);
        boolean isShopMenu = plainTitle.startsWith("Shop Menu");
        boolean isRemovalMenu = plainTitle.startsWith("Your Shop Items");

        if (!isShopMenu && !isRemovalMenu) {
            return;
        }

        event.setCancelled(true);
        
        if (event.getClickedInventory() == null || event.getClickedInventory().equals(player.getInventory())) {
            return;
        }

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.GRAY_STAINED_GLASS_PANE || clickedItem.getType() == Material.BARRIER) {
            return;
        }

        int slot = event.getSlot();
        int currentPage = ShopHandler.getPageFromTitle(title);

        if (slot == 36) {
            if (clickedItem.getType() == Material.ARROW) {
                if (isShopMenu) {
                    player.openInventory(ShopHandler.constructMenu(currentPage - 1));
                } else {
                    player.openInventory(ShopHandler.constructRemovalMenu(player.getUniqueId(), currentPage - 1));
                }
            }
            return;
        }

        if (slot == 44) {
            if (clickedItem.getType() == Material.ARROW) {
                if (isShopMenu) {
                    player.openInventory(ShopHandler.constructMenu(currentPage + 1));
                } else {
                    player.openInventory(ShopHandler.constructRemovalMenu(player.getUniqueId(), currentPage + 1));
                }
            }
            return;
        }

        if (isRemovalMenu) {
            handleRemovalMenuClick(player, slot, clickedItem, currentPage);
            return;
        }

        ShopItem shopItem = ShopHandler.getShopItemAtSlot(slot, currentPage);

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
        player.openInventory(ShopHandler.constructMenu(currentPage));

        player.sendMessage(Component.text("Successfully purchased ", NamedTextColor.GREEN)
                .append(clickedItem.displayName())
                .append(Component.text(" for $" + shopItem.price(), NamedTextColor.GREEN)));
    }

    private void handleRemovalMenuClick(Player player, int slot, ItemStack clickedItem, int currentPage) {
        ShopItem shopItem = ShopHandler.getPlayerItemAtSlot(player.getUniqueId(), slot, currentPage);

        if (shopItem == null) {
            return;
        }

        if (!player.getInventory().addItem(shopItem.getClonedItem()).isEmpty()) {
            player.sendMessage(Component.text("Your inventory is full!", NamedTextColor.RED));
            return;
        }

        ShopHandler.removeItem(shopItem);
        plugin.getDataManager().saveItems();
        player.openInventory(ShopHandler.constructRemovalMenu(player.getUniqueId(), currentPage));

        player.sendMessage(Component.text("Successfully removed ", NamedTextColor.GREEN)
                .append(clickedItem.displayName())
                .append(Component.text(" from the shop", NamedTextColor.GREEN)));
    }

}