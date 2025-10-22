package me.touchie771.shopping.shop;

import me.touchie771.shopping.Shopping;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;
import java.util.logging.Level;

public class ShopDataManager {

    private final Shopping plugin;
    private File itemsFile;
    private FileConfiguration itemsConfig;

    public ShopDataManager(Shopping plugin) {
        this.plugin = plugin;
        setupFiles();
    }

    private void setupFiles() {
        itemsFile = new File(plugin.getDataFolder(), "items.yml");
        if (!itemsFile.exists()) {
            plugin.saveResource("items.yml", false);
        }
        itemsConfig = YamlConfiguration.loadConfiguration(itemsFile);
    }

    public void loadItems() {
        ShopHandler.clearItems();
        
        ConfigurationSection itemsSection = itemsConfig.getConfigurationSection("items");
        if (itemsSection == null) {
            return;
        }

        for (String key : itemsSection.getKeys(false)) {
            try {
                ConfigurationSection itemSection = itemsSection.getConfigurationSection(key);
                if (itemSection == null) {
                    continue;
                }

                ItemStack itemStack = itemSection.getItemStack("item");
                int price = itemSection.getInt("price");
                UUID owner = UUID.fromString(Objects.requireNonNull(itemSection.getString("owner")));

                if (itemStack != null && !itemStack.getType().isAir()) {
                    ShopItem shopItem = new ShopItem(itemStack, price, owner);
                    ShopHandler.addItem(shopItem);
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.WARNING, "Failed to load shop item with key: " + key, e);
            }
        }

        plugin.getLogger().info("Loaded " + ShopHandler.getItems().size() + " shop items from items.yml");
    }

    public void saveItems() {
        itemsConfig.set("items", null);

        for (int i = 0; i < ShopHandler.getItems().size(); i++) {
            ShopItem shopItem = ShopHandler.getItems().get(i);
            String path = "items." + i;

            itemsConfig.set(path + ".item", shopItem.itemStack());
            itemsConfig.set(path + ".price", shopItem.price());
            itemsConfig.set(path + ".owner", shopItem.owner().toString());
        }

        try {
            itemsConfig.save(itemsFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to save items.yml", e);
        }
    }

    public void reload() {
        itemsConfig = YamlConfiguration.loadConfiguration(itemsFile);
        loadItems();
    }
}