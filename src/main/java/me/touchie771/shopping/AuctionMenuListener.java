package me.touchie771.shopping;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public record AuctionMenuListener(Shopping plugin) implements Listener {

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) {
            return;
        }

        Component title = event.getView().title();
        String plainTitle = net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer.plainText().serialize(title);

        if (!plainTitle.startsWith("Active Auctions")) {
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
        int currentPage = AuctionHandler.getPageFromTitle(title);

        if (slot == 36) {
            if (clickedItem.getType() == Material.ARROW) {
                player.openInventory(AuctionHandler.constructAuctionMenu(currentPage - 1));
            }
            return;
        }

        if (slot == 44) {
            if (clickedItem.getType() == Material.ARROW) {
                player.openInventory(AuctionHandler.constructAuctionMenu(currentPage + 1));
            }
            return;
        }

        AuctionItem auction = AuctionHandler.getAuctionAtSlot(slot, currentPage);

        if (auction == null || auction.isExpired()) {
            player.sendMessage(Component.text("This auction has expired!", NamedTextColor.RED));
            player.closeInventory();
            return;
        }

        if (auction.isOwnedBy(player.getUniqueId())) {
            player.sendMessage(Component.text("You cannot bid on your own auction!", NamedTextColor.RED));
            return;
        }

        Economy economy = plugin.getEconomy();
        if (economy == null) {
            player.sendMessage(Component.text("Economy system is not available!", NamedTextColor.RED));
            return;
        }

        int minBid = auction.getCurrentBid() + 1;
        double balance = economy.getBalance(player);

        if (balance < minBid) {
            player.sendMessage(Component.text("You need at least $" + minBid + " to bid! You have $" 
                    + String.format("%.2f", balance), NamedTextColor.RED));
            return;
        }

        if (auction.getCurrentBidder() != null && auction.getCurrentBidder().equals(player.getUniqueId())) {
            player.sendMessage(Component.text("You are already the highest bidder!", NamedTextColor.YELLOW));
            return;
        }

        if (auction.getCurrentBidder() != null) {
            economy.depositPlayer(plugin.getServer().getOfflinePlayer(auction.getCurrentBidder()), 
                                 auction.getCurrentBid());
        }

        economy.withdrawPlayer(player, minBid);

        auction.setCurrentBid(minBid);
        auction.setCurrentBidder(player.getUniqueId());
        plugin.getAuctionDataManager().saveAuctions();

        player.sendMessage(Component.text("Successfully bid $" + minBid + " on ", NamedTextColor.GREEN)
                .append(clickedItem.displayName())
                .append(Component.text("!", NamedTextColor.GREEN)));

        player.openInventory(AuctionHandler.constructAuctionMenu(currentPage));
    }
}