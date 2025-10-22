package me.touchie771.shopping;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SecurityManager {

    private final Shopping plugin;
    private FileConfiguration securityConfig;
    private final Set<Material> blacklistedItems = new HashSet<>();
    private Component blacklistMessage;

    public SecurityManager(Shopping plugin) {
        this.plugin = plugin;
        setupFiles();
        loadBlacklist();
    }

    private void setupFiles() {
        File securityFile = new File(plugin.getDataFolder(), "security.yml");
        if (!securityFile.exists()) {
            plugin.saveResource("security.yml", false);
        }
        securityConfig = YamlConfiguration.loadConfiguration(securityFile);
    }

    private void loadBlacklist() {
        blacklistedItems.clear();

        List<String> items = securityConfig.getStringList("blacklisted_items");
        for (String itemName : items) {
            try {
                Material material = Material.valueOf(itemName.toUpperCase());
                blacklistedItems.add(material);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid material in security.yml: " + itemName);
            }
        }

        String messageString = securityConfig.getString("blacklist_message", "&cThis item is blacklisted and cannot be sold or auctioned!");
        blacklistMessage = LegacyComponentSerializer.legacyAmpersand().deserialize(messageString);

        plugin.getLogger().info("Loaded " + blacklistedItems.size() + " blacklisted items from security.yml");
    }

    public boolean isBlacklisted(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return false;
        }
        return blacklistedItems.contains(item.getType());
    }

    public Component getBlacklistMessage() {
        return blacklistMessage;
    }

}