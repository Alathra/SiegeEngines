package com.github.alathra.siegeengines.command;

import com.github.alathra.siegeengines.SiegeEngine;
import com.github.alathra.siegeengines.config.Config;
import com.github.milkdrinkers.colorparser.ColorParser;
import dev.jorel.commandapi.CommandAPIBukkit;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.ArgumentSuggestions;
import dev.jorel.commandapi.arguments.PlayerArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import dev.jorel.commandapi.exceptions.WrapperCommandSyntaxException;
import dev.jorel.commandapi.executors.CommandArguments;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import static com.github.alathra.siegeengines.SiegeEngines.*;

public class SiegeEnginesCommand {
    public SiegeEnginesCommand() {
        new CommandAPICommand("siegeengines")
            .withFullDescription("Example command.")
            .withShortDescription("Example command.")
            .withPermission("siegeengines.command")
            .withSubcommands(
                commandGet(),
                commandGetAll(),
                commandReload()
            )
            .executesPlayer(this::onExecute)
            .register();
    }

    private void onExecute(CommandSender sender, CommandArguments args) {
        sender.sendMessage(ColorParser.of("<yellow>Incorrect usage, /siegeengines get, /siegeengines getAll, /siegeengines reload").build());
    }

    private CommandAPICommand commandGet() {
        return new CommandAPICommand("get")
            .withPermission("siegeengines.command.get")
            .withArguments(
                new StringArgument("equipmentid")
                    .replaceSuggestions(
                        ArgumentSuggestions.strings(
                            definedSiegeEngines.values().stream().map(SiegeEquipment -> SiegeEquipment.getId()).toList()
                        )
                    ),
                new PlayerArgument("target")
                    .setOptional(true)
            )
            .executesPlayer(this::onGet);
    }

    private CommandAPICommand commandGetAll() {
        return new CommandAPICommand("getAll")
            .withPermission("siegeengines.command.getAll")
            .executesPlayer(this::onGetAll);
    }

    private CommandAPICommand commandReload() {
        return new CommandAPICommand("reload")
            .withPermission("siegeengines.command.reload")
            .executesPlayer(this::onReload);
    }

    private void onGet(CommandSender sender, CommandArguments args) throws WrapperCommandSyntaxException {
        if (!(args.get("equipmentid") instanceof String equipmentId))
            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Invalid SiegeEngine id specified.").build());

        Player player = (Player) args.getOptional("target").orElse(sender);

        for (SiegeEngine SiegeEquipment : definedSiegeEngines.values()) {
            if (SiegeEquipment.equals(equipmentId)) {
                giveSiegeEngine(player, SiegeEquipment);
                break;
            }
        }
    }

    private void onGetAll(CommandSender sender, CommandArguments args) throws WrapperCommandSyntaxException {
        if (!(sender instanceof Player player))
            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Only players can use this command.").build());

        for (SiegeEngine i : definedSiegeEngines.values()) {
            giveSiegeEngine(player, i);
        }
    }

    private void onReload(CommandSender sender, CommandArguments args) throws WrapperCommandSyntaxException {
        if (!(sender instanceof Player))
            throw CommandAPIBukkit.failWithAdventureComponent(ColorParser.of("<red>Only players can use this command.").build());
        Config.reload();
        activeSiegeEngines.clear();
        siegeEngineEntitiesPerPlayer.clear();
        definedSiegeEngines.clear();
        addDefaults();
        for (SiegeEngine i : definedSiegeEngines.values()) {
            if (Config.doDebug) {
                sender.sendMessage(ColorParser.of("<yellow>Enabled SiegeEngine : %s".formatted(i.getEngineName())).build());
                sender.sendMessage(ColorParser.of("<yellow>SiegeEngine Propellant/\"Fuel\" ItemStacks : %s".formatted(i.getFuelItem())).build());
                for (ItemStack proj : i.getProjectiles().keySet()) {
                    sender.sendMessage(ColorParser.of("<yellow>SiegeEngine Projectile ItemStacks : %s".formatted(proj)).build());
                }
            }
        }
        sender.sendMessage(ColorParser.of("<yellow>SiegeEngine configs reloaded").build());
    }

    private void giveSiegeEngine(Player player, SiegeEngine siegeEngine) {
        ItemStack item = new ItemStack(Material.CARVED_PUMPKIN);
        ItemMeta meta = item.getItemMeta();
        meta.setCustomModelData(siegeEngine.getReadyModelNumber());
        meta.setDisplayName(siegeEngine.getItemName());
        meta.setLore(siegeEngine.getItemLore());
        item.setItemMeta(meta);
        player.getInventory().addItem(item);
    }
}