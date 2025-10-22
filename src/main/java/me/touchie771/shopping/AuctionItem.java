package me.touchie771.shopping;

import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class AuctionItem {
    private final UUID auctionId;
    private final ItemStack itemStack;
    private final UUID seller;
    private final int startingBid;
    private final long durationSeconds;
    private final long startTime;
    
    private int currentBid;
    private UUID currentBidder;

    public AuctionItem(ItemStack itemStack, UUID seller, int startingBid, long durationSeconds) {
        this.auctionId = UUID.randomUUID();
        this.itemStack = itemStack.clone();
        this.seller = seller;
        this.startingBid = startingBid;
        this.currentBid = startingBid;
        this.durationSeconds = durationSeconds;
        this.startTime = System.currentTimeMillis();
        this.currentBidder = null;
    }

    public AuctionItem(UUID auctionId, ItemStack itemStack, UUID seller, int startingBid, 
                       long durationSeconds, long startTime, int currentBid, UUID currentBidder) {
        this.auctionId = auctionId;
        this.itemStack = itemStack.clone();
        this.seller = seller;
        this.startingBid = startingBid;
        this.durationSeconds = durationSeconds;
        this.startTime = startTime;
        this.currentBid = currentBid;
        this.currentBidder = currentBidder;
    }

    public UUID getAuctionId() {
        return auctionId;
    }

    public ItemStack getItemStack() {
        return itemStack;
    }

    public ItemStack getClonedItem() {
        return itemStack.clone();
    }

    public UUID getSeller() {
        return seller;
    }

    public int getStartingBid() {
        return startingBid;
    }

    public long getDurationSeconds() {
        return durationSeconds;
    }

    public long getStartTime() {
        return startTime;
    }

    public int getCurrentBid() {
        return currentBid;
    }

    public void setCurrentBid(int currentBid) {
        this.currentBid = currentBid;
    }

    public UUID getCurrentBidder() {
        return currentBidder;
    }

    public void setCurrentBidder(UUID currentBidder) {
        this.currentBidder = currentBidder;
    }

    public boolean hasBids() {
        return currentBidder != null;
    }

    public long getTimeRemainingSeconds() {
        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        return Math.max(0, durationSeconds - elapsed);
    }

    public boolean isExpired() {
        return getTimeRemainingSeconds() <= 0;
    }

    public boolean isOwnedBy(UUID playerId) {
        return seller.equals(playerId);
    }

    public String getTimeRemainingFormatted() {
        long seconds = getTimeRemainingSeconds();
        if (seconds <= 0) {
            return "Expired";
        }
        
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        
        if (hours > 0) {
            return String.format("%dh %dm %ds", hours, minutes, secs);
        } else if (minutes > 0) {
            return String.format("%dm %ds", minutes, secs);
        } else {
            return String.format("%ds", secs);
        }
    }
}