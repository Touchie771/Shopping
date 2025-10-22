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
    private final Set<String> wildcardPatterns = new HashSet<>();
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
        wildcardPatterns.clear();

        List<String> items = securityConfig.getStringList("blacklisted_items");
        for (String itemName : items) {
            itemName = itemName.trim();
            
            if (itemName.startsWith("*")) {
                String pattern = itemName.substring(1).toUpperCase();
                wildcardPatterns.add(pattern);
                plugin.getLogger().info("Loaded wildcard pattern: *" + pattern);
            } else {
                try {
                    Material material = Material.valueOf(itemName.toUpperCase());
                    blacklistedItems.add(material);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid material in security.yml: " + itemName);
                }
            }
        }

        String messageString = securityConfig.getString("blacklist_message", "§cThis item is blacklisted and cannot be sold or auctioned!");
        blacklistMessage = LegacyComponentSerializer.legacySection().deserialize(messageString);

        plugin.getLogger().info("Loaded " + blacklistedItems.size() + " exact blacklisted items and " + wildcardPatterns.size() + " wildcard patterns");
    }

    public boolean isBlacklisted(ItemStack item) {
        if (item == null || item.getType().isAir()) {
            return false;
        }
        
        if (blacklistedItems.contains(item.getType())) {
            return true;
        }
        
        String materialName = item.getType().name();
        for (String pattern : wildcardPatterns) {
            if (materialName.endsWith(pattern)) {
                return true;
            }
        }
        
        return false;
    }

    public Component getBlacklistMessage() {
        return blacklistMessage;
    }
}