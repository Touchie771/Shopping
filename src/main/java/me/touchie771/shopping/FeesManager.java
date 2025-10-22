package me.touchie771.shopping;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

public class FeesManager {

    private final Shopping plugin;
    private FileConfiguration feesConfiguration;

    public FeesManager(Shopping plugin) {
        this.plugin = plugin;
    }

    public void setupFiles() {
        File feesFile = new File(plugin.getDataFolder(), "fees.yml");

        if (!feesFile.exists()) {
            if (plugin.getDataFolder().mkdirs()) {
                try (InputStream inputStream = plugin.getResource("fees.yml")) {
                    if (inputStream != null) {
                        Files.copy(inputStream, feesFile.toPath());
                    }
                } catch (IOException e) {
                    plugin.getLogger().severe("Could not create fees.yml file: " + e.getMessage());
                }
            }
        }

        feesConfiguration = YamlConfiguration.loadConfiguration(feesFile);
    }

    public boolean isEnabled() {
        return feesConfiguration.getBoolean("enabled", true);
    }

    public boolean isListingFeesEnabled() {
        return isEnabled() && feesConfiguration.getBoolean("listing.enabled", true);
    }

    public boolean isSaleTaxEnabled() {
        return !isEnabled() || !feesConfiguration.getBoolean("sale_tax.enabled", true);
    }

    public double getListingFee(double itemPrice) {
        if (!isListingFeesEnabled()) {
            return 0.0;
        }

        double flatFee = feesConfiguration.getDouble("listing.flat_fee", 5.0);
        double percentageFee = feesConfiguration.getDouble("listing.percentage_fee", 0.05);
        double maxPercentageFee = feesConfiguration.getDouble("listing.max_percentage_fee", 100.0);

        double calculatedPercentageFee = itemPrice * percentageFee;
        if (calculatedPercentageFee > maxPercentageFee) {
            calculatedPercentageFee = maxPercentageFee;
        }

        return flatFee + calculatedPercentageFee;
    }

    public double getSaleTax(double salePrice) {
        if (isSaleTaxEnabled()) {
            return 0.0;
        }

        double percentage = feesConfiguration.getDouble("sale_tax.percentage", 0.10);
        double maxTax = feesConfiguration.getDouble("sale_tax.max_tax", 500.0);

        double calculatedTax = salePrice * percentage;
        return Math.min(calculatedTax, maxTax);
    }

    public double getAuctionStartFee() {
        if (!isEnabled()) {
            return 0.0;
        }
        return feesConfiguration.getDouble("auction.start_fee", 2.0);
    }

    public double getAuctionSaleTax(double salePrice) {
        if (isSaleTaxEnabled()) {
            return 0.0;
        }

        double percentage = feesConfiguration.getDouble("auction.sale_tax_percentage", 0.08);
        double maxTax = feesConfiguration.getDouble("sale_tax.max_tax", 500.0);

        double calculatedTax = salePrice * percentage;
        return Math.min(calculatedTax, maxTax);
    }

}