package io.github.alathra.siegeengines.command;

import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import io.github.alathra.siegeengines.SiegeEngines;
import io.github.alathra.siegeengines.Reloadable;

/**
 * A class to handle registration of commands.
 */
public class CommandHandler implements Reloadable {
    public static final String BASE_PERM = "example.command";
    private final SiegeEngines plugin;

    /**
     * Instantiates the Command handler.
     *
     * @param plugin the plugin
     */
    public CommandHandler(SiegeEngines plugin) {
        this.plugin = plugin;
    }

    @Override
    public void onLoad(SiegeEngines plugin) {
        CommandAPI.onLoad(
            new CommandAPIBukkitConfig(plugin)
                .shouldHookPaperReload(true)
                .silentLogs(true)
                .beLenientForMinorVersions(true)
        );
    }

    @Override
    public void onEnable(SiegeEngines plugin) {
        if (!CommandAPI.isLoaded())
            return;

        CommandAPI.onEnable();

        // Register commands here
        new ExampleCommand();
    }

    @Override
    public void onDisable(SiegeEngines plugin) {
        if (!CommandAPI.isLoaded())
            return;

        CommandAPI.getRegisteredCommands().forEach(registeredCommand -> CommandAPI.unregister(registeredCommand.namespace() + ':' + registeredCommand.commandName(), true));
        CommandAPI.onDisable();
    }
}