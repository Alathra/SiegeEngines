package com.github.alathra.siegeengines.command;

import com.github.alathra.siegeengines.SiegeEngines;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;

/**
 * A class to handle registration of commands.
 */
public class CommandHandler {
    private final SiegeEngines instance;

    public CommandHandler(SiegeEngines instance) {
        this.instance = instance;
    }

    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(instance).shouldHookPaperReload(true).silentLogs(true));
    }

    public void onEnable() {
        CommandAPI.onEnable();

        // Register commands here
        new SiegeEnginesCommand();
    }

    public void onDisable() {
        CommandAPI.onDisable();
    }
}