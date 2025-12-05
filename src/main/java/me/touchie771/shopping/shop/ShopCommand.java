package me.touchie771.shopping.shop;

import me.touchie771.minecraftCommands.api.annotations.Execute;
import me.touchie771.minecraftCommands.api.annotations.Permission;
import me.touchie771.minecraftCommands.api.annotations.Command;
import me.touchie771.shopping.Shopping;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Command(
    name = "shop",
    description = "Open the shop menu",
    usage = "/shop [sell|remove] [price]"
)
@Permission("shopping.menu")
public class ShopCommand {

    private static Shopping plugin;

    public ShopCommand() {
        // Default constructor for library instantiation
    }

    public static void setPlugin(Shopping pluginInstance) {
        plugin = pluginInstance;
    }

    @Execute
    public void execute(Player player, String[] args) {
        if (args.length == 0) {
            // Open shop menu
            player.openInventory(ShopHandler.constructMenu());
            return;
        }

        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "sell" -> {
                if (!player.hasPermission("shopping.sell")) {
                    player.sendMessage(Component.text("You don't have permission to use this command!", NamedTextColor.RED));
                    return;
                }
                handleSell(player, args);
            }
            case "remove" -> {
                if (!player.hasPermission("shopping.remove")) {
                    player.sendMessage(Component.text("You don't have permission to use this command!", NamedTextColor.RED));
                    return;
                }
                player.openInventory(ShopHandler.constructRemovalMenu(player.getUniqueId()));
            }
            default -> {
                player.sendMessage(Component.text("Usage: /shop [sell|remove] [price]", NamedTextColor.RED));
            }
        }
    }

    private void handleSell(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(Component.text("Usage: /shop sell <price>", NamedTextColor.RED));
            return;
        }

        int price;
        try {
            price = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            player.sendMessage(Component.text("Price must be a valid number!", NamedTextColor.RED));
            return;
        }

        ItemStack heldItem = player.getInventory().getItemInMainHand();

        if (heldItem.getType() == Material.AIR || heldItem.getAmount() == 0) {
            player.sendMessage(Component.text("You must be holding an item to sell!", NamedTextColor.RED));
            return;
        }

        if (plugin.getSecurityManager().isBlacklisted(heldItem)) {
            player.sendMessage(plugin.getSecurityManager().getBlacklistMessage());
            return;
        }

        if (price <= 0) {
            player.sendMessage(Component.text("Price must be greater than 0!", NamedTextColor.RED));
            return;
        }

        // Check listing fee
        double listingFee = plugin.getFeesManager().getListingFee(price);
        if (listingFee > 0) {
            Economy economy = plugin.getEconomy();
            double balance = economy.getBalance(player);
            if (balance < listingFee) {
                player.sendMessage(Component.text("You don't have enough money for the listing fee! Need $" +
                        String.format("%.2f", listingFee) + " but only have $" + String.format("%.2f", balance),
                        NamedTextColor.RED));
                return;
            }
            economy.withdrawPlayer(player, listingFee);
            player.sendMessage(Component.text("Listing fee: $" + String.format("%.2f", listingFee), NamedTextColor.YELLOW));
        }

        ItemStack itemToSell = heldItem.clone();

        ShopItem shopItem = new ShopItem(itemToSell, price, player.getUniqueId());
        ShopHandler.addItem(shopItem);
        plugin.getDataManager().saveItems();

        player.getInventory().setItemInMainHand(null);

        player.sendMessage(Component.text("Successfully listed ", NamedTextColor.GREEN)
                .append(Component.text(itemToSell.getAmount() + "x ", NamedTextColor.YELLOW))
                .append(itemToSell.displayName())
                .append(Component.text(" for $" + price, NamedTextColor.GREEN)));
    }
}