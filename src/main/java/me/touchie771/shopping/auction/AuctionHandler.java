package me.touchie771.shopping.auction;

import me.touchie771.shopping.Shopping;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;

public class AuctionHandler {

    private static final List<AuctionItem> activeAuctions = new ArrayList<>();
    private static final int ITEMS_PER_PAGE = 36;

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

    public static Inventory constructAuctionMenu(int page) {
        List<AuctionItem> sortedAuctions = activeAuctions.stream()
                .sorted(Comparator.comparingLong(AuctionItem::getTimeRemainingSeconds))
                .toList();

        int totalPages = (int) Math.ceil((double) sortedAuctions.size() / ITEMS_PER_PAGE);
        if (totalPages == 0) totalPages = 1;
        if (page < 1) page = 1;
        if (page > totalPages) page = totalPages;

        Inventory auctionMenu = Bukkit.createInventory(null, 45, 
                Component.text("Active Auctions - Page " + page + "/" + totalPages,
                        NamedTextColor.GOLD, TextDecoration.BOLD));

        int startIndex = (page - 1) * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, sortedAuctions.size());

        for (int i = startIndex; i < endIndex; i++) {
            AuctionItem auction = sortedAuctions.get(i);
            ItemStack displayItem = createAuctionDisplayItem(auction);
            auctionMenu.setItem(i - startIndex, displayItem);
        }

        addNavigationButtons(auctionMenu, page, totalPages);
        addFillerItem(auctionMenu);

        return auctionMenu;
    }

    public static Inventory constructAuctionMenu() {
        return constructAuctionMenu(1);
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

    public static AuctionItem getAuctionAtSlot(int slot, int page) {
        List<AuctionItem> sortedAuctions = activeAuctions.stream()
                .sorted(Comparator.comparingLong(AuctionItem::getTimeRemainingSeconds))
                .toList();

        int actualIndex = (page - 1) * ITEMS_PER_PAGE + slot;
        if (actualIndex < 0 || actualIndex >= sortedAuctions.size()) {
            return null;
        }
        return sortedAuctions.get(actualIndex);
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

    private static void addFillerItem(Inventory inventory) {
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