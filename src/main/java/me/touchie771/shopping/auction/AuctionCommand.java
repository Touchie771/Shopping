package me.touchie771.shopping.auction;

import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import me.touchie771.shopping.Shopping;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

@Command(name = "auction", aliases = {"auc"})
@Permission("shopping.auction")
public record AuctionCommand(Shopping plugin) {

    @Execute
    public void execute(@Context Player player) {
        player.openInventory(AuctionHandler.constructAuctionMenu());
    }

    @Execute(name = "start")
    @Permission("shopping.auction.start")
    public void start(@Context Player player, @Arg int startingBid, @Arg int durationMinutes) {
        ItemStack heldItem = player.getInventory().getItemInMainHand();

        if (heldItem.getType() == Material.AIR || heldItem.getAmount() == 0) {
            player.sendMessage(Component.text("You must be holding an item to auction!", NamedTextColor.RED));
            return;
        }

        if (plugin.getSecurityManager().isBlacklisted(heldItem)) {
            player.sendMessage(plugin.getSecurityManager().getBlacklistMessage());
            return;
        }

        if (startingBid <= 0) {
            player.sendMessage(Component.text("Starting bid must be greater than 0!", NamedTextColor.RED));
            return;
        }

        if (durationMinutes < 1 || durationMinutes > 1440) {
            player.sendMessage(Component.text("Duration must be between 1 and 1440 minutes (24 hours)!", NamedTextColor.RED));
            return;
        }

        // Check fees
        double listingFee = plugin.getFeesManager().getListingFee(startingBid);
        double auctionStartFee = plugin.getFeesManager().getAuctionStartFee();
        double totalFee = listingFee + auctionStartFee;

        if (totalFee > 0) {
            Economy economy = plugin.getEconomy();
            if (economy == null) {
                player.sendMessage(Component.text("Economy system is not available!", NamedTextColor.RED));
                return;
            }
            
            double balance = economy.getBalance(player);
            if (balance < totalFee) {
                player.sendMessage(Component.text("You don't have enough money for the fees! Need $" +
                        String.format("%.2f", totalFee) + " but only have $" + String.format("%.2f", balance),
                        NamedTextColor.RED));
                return;
            }
            economy.withdrawPlayer(player, totalFee);

            Component feeMessage = getFeeMessage(listingFee, auctionStartFee);
            player.sendMessage(feeMessage);
        }

        ItemStack itemToAuction = heldItem.clone();
        long durationSeconds = durationMinutes * 60L;

        AuctionItem auction = new AuctionItem(itemToAuction, player.getUniqueId(), startingBid, durationSeconds);
        AuctionHandler.addAuction(auction);
        plugin.getAuctionDataManager().saveAuctions();

        player.getInventory().setItemInMainHand(null);

        player.sendMessage(Component.text("Successfully started auction for ", NamedTextColor.GREEN)
                .append(Component.text(itemToAuction.getAmount() + "x ", NamedTextColor.YELLOW))
                .append(itemToAuction.displayName())
                .append(Component.text(" with starting bid $" + startingBid, NamedTextColor.GREEN))
                .append(Component.text(" for " + durationMinutes + " minutes", NamedTextColor.GREEN)));
    }

    private static @NotNull Component getFeeMessage(double listingFee, double auctionStartFee) {
        Component feeMessage = Component.text("Fees charged: ", NamedTextColor.YELLOW);
        if (listingFee > 0) {
            feeMessage = feeMessage.append(Component.text("listing $" + String.format("%.2f", listingFee), NamedTextColor.YELLOW));
        }
        if (auctionStartFee > 0) {
            if (listingFee > 0) feeMessage = feeMessage.append(Component.text(" + ", NamedTextColor.YELLOW));
            feeMessage = feeMessage.append(Component.text("auction $" + String.format("%.2f", auctionStartFee), NamedTextColor.YELLOW));
        }
        return feeMessage;
    }

    @Execute(name = "bid")
    @Permission("shopping.auction.bid")
    public void bid(@Context Player player, @Arg int bidAmount) {
        if (bidAmount <= 0) {
            player.sendMessage(Component.text("Bid amount must be greater than 0!", NamedTextColor.RED));
            return;
        }

        player.sendMessage(Component.text("Please click on an auction in the menu to bid on it!", NamedTextColor.YELLOW));
        player.openInventory(AuctionHandler.constructAuctionMenu());
    }

    @Execute(name = "list")
    @Permission("shopping.auction.list")
    public void list(@Context Player player) {
        if (AuctionHandler.getActiveAuctions().isEmpty()) {
            player.sendMessage(Component.text("There are no active auctions!", NamedTextColor.YELLOW));
            return;
        }

        player.sendMessage(Component.text("=== Active Auctions ===", NamedTextColor.GOLD));

        for (AuctionItem auction : AuctionHandler.getActiveAuctions()) {
            Component message = Component.text("- ", NamedTextColor.GRAY)
                    .append(auction.getItemStack().displayName())
                    .append(Component.text(" | Bid: $" + auction.getCurrentBid(), NamedTextColor.GREEN))
                    .append(Component.text(" | Time: " + auction.getTimeRemainingFormatted(), NamedTextColor.YELLOW));
            player.sendMessage(message);
        }
    }

    @Execute(name = "cancel")
    @Permission("shopping.auction.cancel")
    public void cancel(@Context Player player) {
        var playerAuctions = AuctionHandler.getAuctionsBySeller(player.getUniqueId());

        if (playerAuctions.isEmpty()) {
            player.sendMessage(Component.text("You have no active auctions!", NamedTextColor.RED));
            return;
        }

        for (AuctionItem auction : playerAuctions) {
            if (auction.hasBids()) {
                Economy economy = plugin.getEconomy();
                economy.depositPlayer(plugin.getServer().getOfflinePlayer(auction.getCurrentBidder()),
                        auction.getCurrentBid());
            }

            player.getInventory().addItem(auction.getClonedItem());
            AuctionHandler.removeAuction(auction);
        }

        plugin.getAuctionDataManager().saveAuctions();
        player.sendMessage(Component.text("Cancelled " + playerAuctions.size() + " auction(s) and returned items!", NamedTextColor.GREEN));
    }

    @Execute(name = "claim")
    @Permission("shopping.auction.claim")
    public void claim(@Context Player player) {
        var pendingItems = plugin.getPendingItems().get(player.getUniqueId());

        if (pendingItems == null || pendingItems.isEmpty()) {
            player.sendMessage(Component.text("You have no items to claim!", NamedTextColor.YELLOW));
            return;
        }

        int claimed = 0;
        for (ItemStack item : new ArrayList<>(pendingItems)) {
            if (player.getInventory().addItem(item).isEmpty()) {
                pendingItems.remove(item);
                claimed++;
            }
        }

        if (claimed > 0) {
            plugin.getAuctionDataManager().saveAuctions();
            player.sendMessage(Component.text("Claimed " + claimed + " item(s)!", NamedTextColor.GREEN));
        } else {
            player.sendMessage(Component.text("Your inventory is full!", NamedTextColor.RED));
        }
    }
}