package me.touchie771.shopping;

import dev.rollczi.litecommands.annotations.argument.Arg;
import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

@Command(name = "shop")
@Permission("shopping.menu")
public record ShopCommand(Shopping plugin) {

    @Execute
    public void execute(@Context Player player) {
        player.openInventory(ShopHandler.constructMenu());
    }

    @Execute(name = "sell")
    @Permission("shopping.sell")
    public void sell(@Context Player player, @Arg int price) {
        ItemStack heldItem = player.getInventory().getItemInMainHand();

        if (heldItem.getType() == Material.AIR || heldItem.getAmount() == 0) {
            player.sendMessage(Component.text("You must be holding an item to sell!", NamedTextColor.RED));
            return;
        }

        if (price <= 0) {
            player.sendMessage(Component.text("Price must be greater than 0!", NamedTextColor.RED));
            return;
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

    @Execute(name = "remove")
    @Permission("shopping.remove")
    public void remove(@Context Player player) {
        player.openInventory(ShopHandler.constructRemovalMenu(player.getUniqueId()));
    }
}