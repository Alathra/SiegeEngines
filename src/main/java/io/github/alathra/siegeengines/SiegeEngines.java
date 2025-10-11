package io.github.alathra.siegeengines;

import io.github.alathra.siegeengines.command.CommandHandler;
import io.github.alathra.siegeengines.config.ConfigHandler;
import io.github.alathra.siegeengines.cooldown.CooldownHandler;
import io.github.alathra.siegeengines.hook.HookManager;
import io.github.alathra.siegeengines.listener.ListenerHandler;
import io.github.alathra.siegeengines.threadutil.SchedulerHandler;
import io.github.alathra.siegeengines.translation.TranslationHandler;
import io.github.alathra.siegeengines.updatechecker.UpdateHandler;
import io.github.alathra.siegeengines.utility.Logger;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * Main class.
 */
@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class SiegeEngines extends JavaPlugin {
    private static SiegeEngines instance;

    // Handlers/Managers
    private ConfigHandler configHandler;
    private TranslationHandler translationHandler;
    private HookManager hookManager;
    private CommandHandler commandHandler;
    private ListenerHandler listenerHandler;
    private UpdateHandler updateHandler;
    private SchedulerHandler schedulerHandler;
    private CooldownHandler cooldownHandler;

    // Handlers list (defines order of load/enable/disable)
    private List<? extends Reloadable> handlers;

    /**
     * Gets plugin instance.
     *
     * @return the plugin instance
     */
    public static SiegeEngines getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;

        configHandler = new ConfigHandler(this);
        translationHandler = new TranslationHandler(configHandler);
        hookManager = new HookManager(this);
        commandHandler = new CommandHandler(this);
        listenerHandler = new ListenerHandler(this);
        updateHandler = new UpdateHandler(this);
        schedulerHandler = new SchedulerHandler();
        cooldownHandler = new CooldownHandler();

        handlers = List.of(
            configHandler,
            translationHandler,
            hookManager,
            commandHandler,
            listenerHandler,
            updateHandler,
            schedulerHandler,
            cooldownHandler
        );

        for (Reloadable handler : handlers)
            handler.onLoad(instance);
    }

    @Override
    public void onEnable() {
        for (Reloadable handler : handlers)
            handler.onEnable(instance);
    }

    @Override
    public void onDisable() {
        for (Reloadable handler : handlers.reversed()) // If reverse doesn't work implement a new List with your desired disable order
            handler.onDisable(instance);
    }

    /**
     * Use to reload the entire plugin.
     */
    public void onReload() {
        onDisable();
        onLoad();
        onEnable();
    }

    /**
     * Gets config handler.
     *
     * @return the config handler
     */
    @NotNull
    public ConfigHandler getConfigHandler() {
        return configHandler;
    }

    /**
     * Gets hook manager.
     *
     * @return the hook manager
     */
    @NotNull
    public HookManager getHookManager() {
        return hookManager;
    }

    /**
     * Gets update handler.
     *
     * @return the update handler
     */
    @NotNull
    public UpdateHandler getUpdateHandler() {
        return updateHandler;
    }
}
