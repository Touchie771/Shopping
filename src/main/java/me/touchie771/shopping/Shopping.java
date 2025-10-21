package me.touchie771.shopping;

import dev.rollczi.litecommands.LiteCommands;
import dev.rollczi.litecommands.bukkit.LiteBukkitFactory;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class Shopping extends JavaPlugin {

    private Economy economy;
    private LiteCommands<CommandSender> liteCommands;
    private ShopDataManager dataManager;

    @Override
    public void onEnable() {
        setupEconomy();
        setupDataManager();
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

    private void setupDataManager() {
        dataManager = new ShopDataManager(this);
        dataManager.loadItems();
    }

    private void setupCommands() {
        liteCommands = LiteBukkitFactory.builder()
                .commands(new ShopCommand(this))
                .build();
    }

    private void setupListeners() {
        getServer().getPluginManager().registerEvents(new ShopMenuListener(this), this);
    }

    public Economy getEconomy() {
        return economy;
    }

    public ShopDataManager getDataManager() {
        return dataManager;
    }
}