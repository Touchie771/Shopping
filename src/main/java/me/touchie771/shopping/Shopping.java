package me.touchie771.shopping;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class Shopping extends JavaPlugin {

    private static Economy economy = null;

    @Override
    public void onEnable() {
        // Plugin startup logic
        setupEconomy();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    private void setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            getLogger().severe("Disabled due to no Vault dependency found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        economy = rsp.getProvider();
        if (economy == null) {
            getLogger().severe("Disabled due to unable to get Vault dependency!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getLogger().info("Vault economy successfully hooked!");
    }

    public static Economy getEconomy() {
        return economy;
    }
}