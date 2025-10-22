package me.touchie771.shopping;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class AuctionHandler {

    private static final List<AuctionItem> activeAuctions = new ArrayList<>();

    public static List<AuctionItem> getActiveAuctions() {
        return activeAuctions;
    }

    public static void addAuction(AuctionItem auction) {
        activeAuctions.add(auction);
    }

    public static void removeAuction(AuctionItem auction) {
        activeAuctions.remove(auction);
    }

    public static void clearAuctions() {
        activeAuctions.clear();
    }

    public static List<AuctionItem> getAuctionsBySeller(UUID sellerId) {
        return activeAuctions.stream()
                .filter(auction -> auction.isOwnedBy(sellerId))
                .toList();
    }

    public static Inventory constructAuctionMenu() {
        Inventory auctionMenu = Bukkit.createInventory(null, 54, 
                Component.text("Active Auctions", NamedTextColor.GOLD));

        List<AuctionItem> sortedAuctions = activeAuctions.stream()
                .sorted(Comparator.comparingLong(AuctionItem::getTimeRemainingSeconds))
                .toList();

        for (int i = 0; i < sortedAuctions.size() && i < 54; i++) {
            AuctionItem auction = sortedAuctions.get(i);
            ItemStack displayItem = createAuctionDisplayItem(auction);
            auctionMenu.setItem(i, displayItem);
        }

        return auctionMenu;
    }

    public static ItemStack createAuctionDisplayItem(AuctionItem auction) {
        ItemStack displayItem = auction.getClonedItem();
        ItemMeta meta = displayItem.getItemMeta();

        if (meta != null) {
            List<Component> lore = meta.lore() != null ? new ArrayList<>(Objects.requireNonNull(meta.lore())) : new ArrayList<>();

            lore.add(Component.empty());
            
            OfflinePlayer seller = Bukkit.getOfflinePlayer(auction.getSeller());
            String sellerName = seller.getName() != null ? seller.getName() : "Unknown";
            lore.add(Component.text("Seller: ", NamedTextColor.GRAY)
                    .append(Component.text(sellerName, NamedTextColor.YELLOW)));

            lore.add(Component.text("Starting Bid: ", NamedTextColor.GRAY)
                    .append(Component.text("$" + auction.getStartingBid(), NamedTextColor.GREEN)));

            if (auction.hasBids()) {
                lore.add(Component.text("Current Bid: ", NamedTextColor.GRAY)
                        .append(Component.text("$" + auction.getCurrentBid(), NamedTextColor.GOLD)));
                
                OfflinePlayer bidder = Bukkit.getOfflinePlayer(auction.getCurrentBidder());
                String bidderName = bidder.getName() != null ? bidder.getName() : "Unknown";
                lore.add(Component.text("Highest Bidder: ", NamedTextColor.GRAY)
                        .append(Component.text(bidderName, NamedTextColor.AQUA)));
            } else {
                lore.add(Component.text("Current Bid: ", NamedTextColor.GRAY)
                        .append(Component.text("No bids yet", NamedTextColor.DARK_GRAY)));
            }

            lore.add(Component.text("Time Remaining: ", NamedTextColor.GRAY)
                    .append(Component.text(auction.getTimeRemainingFormatted(), NamedTextColor.RED)));

            lore.add(Component.empty());
            lore.add(Component.text("Click to place a bid!", NamedTextColor.LIGHT_PURPLE));

            meta.lore(lore);
            displayItem.setItemMeta(meta);
        }

        return displayItem;
    }

    public static void removeExpiredAuctions(Shopping plugin) {
        List<AuctionItem> expired = activeAuctions.stream()
                .filter(AuctionItem::isExpired)
                .toList();

        for (AuctionItem auction : expired) {
            completeAuction(auction, plugin);
        }
    }

    public static void completeAuction(AuctionItem auction, Shopping plugin) {
        if (auction.hasBids()) {
            OfflinePlayer winner = Bukkit.getOfflinePlayer(auction.getCurrentBidder());
            OfflinePlayer seller = Bukkit.getOfflinePlayer(auction.getSeller());

            plugin.getEconomy().depositPlayer(seller, auction.getCurrentBid());

            if (winner.isOnline()) {
                Objects.requireNonNull(winner.getPlayer()).getInventory().addItem(auction.getClonedItem());
                winner.getPlayer().sendMessage(Component.text("You won the auction for ", NamedTextColor.GREEN)
                        .append(auction.getItemStack().displayName())
                        .append(Component.text("!", NamedTextColor.GREEN)));
            } else {
                plugin.getPendingItems().computeIfAbsent(auction.getCurrentBidder(), k -> new ArrayList<>())
                        .add(auction.getClonedItem());
            }

            if (seller.isOnline()) {
                Objects.requireNonNull(seller.getPlayer()).sendMessage(Component.text("Your auction for ", NamedTextColor.GREEN)
                        .append(auction.getItemStack().displayName())
                        .append(Component.text(" sold for $" + auction.getCurrentBid(), NamedTextColor.GREEN)));
            }
        } else {
            OfflinePlayer seller = Bukkit.getOfflinePlayer(auction.getSeller());
            
            if (seller.isOnline()) {
                Objects.requireNonNull(seller.getPlayer()).getInventory().addItem(auction.getClonedItem());
                seller.getPlayer().sendMessage(Component.text("Your auction expired with no bids. Item returned.", NamedTextColor.YELLOW));
            } else {
                plugin.getPendingItems().computeIfAbsent(auction.getSeller(), k -> new ArrayList<>())
                        .add(auction.getClonedItem());
            }
        }

        removeAuction(auction);
    }
}