package me.touchie771.shopping.auction;

import me.touchie771.shopping.Shopping;
import org.bukkit.scheduler.BukkitRunnable;

public class AuctionTask extends BukkitRunnable {

    private final Shopping plugin;

    public AuctionTask(Shopping plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        AuctionHandler.removeExpiredAuctions(plugin);
        plugin.getAuctionDataManager().saveAuctions();
    }
}