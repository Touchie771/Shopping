package me.touchie771.shopping;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public final class Shopping extends JavaPlugin {

    private Economy economy;

    @Override
    public void onEnable() {
        setupEconomy();
    }

    private void setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            getLogger().severe("No Vault economy provider found!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        economy = rsp.getProvider();
        if (economy == null) {
            getLogger().severe("Failed to get Vault economy provider!");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }
        getLogger().info("Vault economy hooked successfully!");
    }

    public Economy getEconomy() {
        return economy;
    }
}