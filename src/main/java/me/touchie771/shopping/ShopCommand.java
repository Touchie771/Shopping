package me.touchie771.shopping;

import dev.rollczi.litecommands.annotations.command.Command;
import dev.rollczi.litecommands.annotations.context.Context;
import dev.rollczi.litecommands.annotations.execute.Execute;
import dev.rollczi.litecommands.annotations.permission.Permission;
import org.bukkit.entity.Player;

@Command(name = "shop")
@Permission("shopping.menu")
public record ShopCommand() {

    @Execute
    public void execute(@Context Player player) {
        player.sendMessage("Shop command executed!");
    }
}