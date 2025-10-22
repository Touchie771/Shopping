package me.touchie771.shopping;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

public class AuctionDataManager {

    private final Shopping plugin;
    private File auctionsFile;
    private FileConfiguration auctionsConfig;
    private File pendingItemsFile;
    private FileConfiguration pendingItemsConfig;

    public AuctionDataManager(Shopping plugin) {
        this.plugin = plugin;
        setupFiles();
    }

    private void setupFiles() {
        auctionsFile = new File(plugin.getDataFolder(), "auctions.yml");
        if (!auctionsFile.exists()) {
            plugin.saveResource("auctions.yml", false);
        }
        auctionsConfig = YamlConfiguration.loadConfiguration(auctionsFile);

        pendingItemsFile = new File(plugin.getDataFolder(), "pending_items.yml");
        if (!pendingItemsFile.exists()) {
            plugin.saveResource("pending_items.yml", false);
        }
        pendingItemsConfig = YamlConfiguration.loadConfiguration(pendingItemsFile);
    }

    public void loadAuctions() {
        AuctionHandler.clearAuctions();

        ConfigurationSection auctionsSection = auctionsConfig.getConfigurationSection("auctions");
        if (auctionsSection == null) {
            return;
        }

        for (String key : auctionsSection.getKeys(false)) {
            try {
                ConfigurationSection auctionSection = auctionsSection.getConfigurationSection(key);
                if (auctionSection == null) {
                    continue;
                }

                UUID auctionId = UUID.fromString(Objects.requireNonNull(auctionSection.getString("id")));
                ItemStack itemStack = auctionSection.getItemStack("item");
                UUID seller = UUID.fromString(Objects.requireNonNull(auctionSection.getString("seller")));
                int startingBid = auctionSection.getInt("startingBid");
                long durationSeconds = auctionSection.getLong("durationSeconds");
                long startTime = auctionSection.getLong("startTime");
                int currentBid = auctionSection.getInt("currentBid");
                UUID currentBidder = auctionSection.contains("currentBidder") 
                        ? UUID.fromString(Objects.requireNonNull(auctionSection.getString("currentBidder")))
                        : null;

                if (itemStack != null && !itemStack.getType().isAir()) {
                    AuctionItem auction = new AuctionItem(auctionId, itemStack, seller, startingBid, 
                                                          durationSeconds, startTime, currentBid, currentBidder);
                    
                    if (!auction.isExpired()) {
                        AuctionHandler.addAuction(auction);
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to load auction with key: " + key, e);
            }
        }

        plugin.getLogger().info("Loaded " + AuctionHandler.getActiveAuctions().size() + " active auctions from auctions.yml");
    }

    public void saveAuctions() {
        auctionsConfig.set("auctions", null);

        for (int i = 0; i < AuctionHandler.getActiveAuctions().size(); i++) {
            AuctionItem auction = AuctionHandler.getActiveAuctions().get(i);
            String path = "auctions." + i;

            auctionsConfig.set(path + ".id", auction.getAuctionId().toString());
            auctionsConfig.set(path + ".item", auction.getItemStack());
            auctionsConfig.set(path + ".seller", auction.getSeller().toString());
            auctionsConfig.set(path + ".startingBid", auction.getStartingBid());
            auctionsConfig.set(path + ".durationSeconds", auction.getDurationSeconds());
            auctionsConfig.set(path + ".startTime", auction.getStartTime());
            auctionsConfig.set(path + ".currentBid", auction.getCurrentBid());
            if (auction.getCurrentBidder() != null) {
                auctionsConfig.set(path + ".currentBidder", auction.getCurrentBidder().toString());
            }
        }

        try {
            auctionsConfig.save(auctionsFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save auctions.yml", e);
        }

        savePendingItems();
    }

    public void loadPendingItems() {
        plugin.getPendingItems().clear();

        ConfigurationSection itemsSection = pendingItemsConfig.getConfigurationSection("pending");
        if (itemsSection == null) {
            return;
        }

        for (String uuidString : itemsSection.getKeys(false)) {
            try {
                UUID playerId = UUID.fromString(uuidString);
                var itemsList = itemsSection.getList(uuidString);
                
                if (itemsList != null) {
                    ArrayList<ItemStack> items = new ArrayList<>();
                    for (Object obj : itemsList) {
                        if (obj instanceof ItemStack itemStack) {
                            items.add(itemStack);
                        }
                    }
                    if (!items.isEmpty()) {
                        plugin.getPendingItems().put(playerId, items);
                    }
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to load pending items for: " + uuidString, e);
            }
        }

        plugin.getLogger().info("Loaded pending items for " + plugin.getPendingItems().size() + " players");
    }

    public void savePendingItems() {
        pendingItemsConfig.set("pending", null);

        for (var entry : plugin.getPendingItems().entrySet()) {
            pendingItemsConfig.set("pending." + entry.getKey().toString(), entry.getValue());
        }

        try {
            pendingItemsConfig.save(pendingItemsFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save pending_items.yml", e);
        }
    }

}