package me.touchie771.shopping;

import dev.rollczi.litecommands.LiteCommands;
import dev.rollczi.litecommands.bukkit.LiteBukkitFactory;
import me.touchie771.shopping.auction.AuctionCommand;
import me.touchie771.shopping.auction.AuctionDataManager;
import me.touchie771.shopping.auction.AuctionMenuListener;
import me.touchie771.shopping.auction.AuctionTask;
import me.touchie771.shopping.shop.ShopCommand;
import me.touchie771.shopping.shop.ShopDataManager;
import me.touchie771.shopping.shop.ShopMenuListener;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.CommandSender;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class Shopping extends JavaPlugin {

    private Economy economy;
    private LiteCommands<CommandSender> liteCommands;
    private ShopDataManager dataManager;
    private AuctionDataManager auctionDataManager;
    private SecurityManager securityManager;
    private BukkitTask auctionTask;
    private final Map<UUID, ArrayList<ItemStack>> pendingItems = new HashMap<>();

    @Override
    public void onEnable() {
        setupEconomy();
        setupSecurity();
        setupDataManager();
        setupAuctionSystem();
        setupCommands();
        setupListeners();
    }

    @Override
    public void onDisable() {
        if (liteCommands != null) {
            liteCommands.unregister();
        }
        if (dataManager != null) {
            dataManager.saveItems();
        }
        if (auctionTask != null) {
            auctionTask.cancel();
        }
        if (auctionDataManager != null) {
            auctionDataManager.saveAuctions();
        }
    }

    private void setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            getLogger().severe("No Vault economy provider found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        economy = rsp.getProvider();
        getLogger().info("Vault economy hooked successfully!");
    }

    private void setupSecurity() {
        securityManager = new SecurityManager(this);
    }

    private void setupDataManager() {
        dataManager = new ShopDataManager(this);
        dataManager.loadItems();
    }

    private void setupAuctionSystem() {
        auctionDataManager = new AuctionDataManager(this);
        auctionDataManager.loadAuctions();
        auctionDataManager.loadPendingItems();
        
        auctionTask = new AuctionTask(this).runTaskTimer(this, 20L, 20L);
    }

    private void setupCommands() {
        liteCommands = LiteBukkitFactory.builder()
                .commands(new ShopCommand(this), new AuctionCommand(this))
                .build();
    }

    private void setupListeners() {
        getServer().getPluginManager().registerEvents(new ShopMenuListener(this), this);
        getServer().getPluginManager().registerEvents(new AuctionMenuListener(this), this);
    }

    public Economy getEconomy() {
        return economy;
    }

    public ShopDataManager getDataManager() {
        return dataManager;
    }

    public AuctionDataManager getAuctionDataManager() {
        return auctionDataManager;
    }

    public Map<UUID, ArrayList<ItemStack>> getPendingItems() {
        return pendingItems;
    }

    public SecurityManager getSecurityManager() {
        return securityManager;
    }
}