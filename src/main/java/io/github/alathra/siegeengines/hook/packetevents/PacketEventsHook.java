package io.github.alathra.siegeengines.hook.packetevents;

import com.github.retrooper.packetevents.PacketEvents;
import io.github.alathra.siegeengines.SiegeEngines;
import io.github.alathra.siegeengines.hook.AbstractHook;
import io.github.alathra.siegeengines.hook.Hook;
import io.github.retrooper.packetevents.factory.spigot.SpigotPacketEventsBuilder;

/**
 * A hook that enables API for PacketEvents.
 */
public class PacketEventsHook extends AbstractHook {
    /**
     * Instantiates a new PacketEvents hook.
     *
     * @param plugin the plugin instance
     */
    public PacketEventsHook(SiegeEngines plugin) {
        super(plugin);
    }

    @Override
    public void onLoad(SiegeEngines plugin) {
        if (!isPluginPresent(Hook.PacketEvents.getPluginName()))
            return;

        PacketEvents.setAPI(SpigotPacketEventsBuilder.build(getPlugin()));
        PacketEvents.getAPI().load();
    }

    @Override
    public void onEnable(SiegeEngines plugin) {
        if (!isPluginEnabled(Hook.PacketEvents.getPluginName()))
            return;

        PacketEvents.getAPI().init();
    }

    @Override
    public void onDisable(SiegeEngines plugin) {
        if (!isPluginEnabled(Hook.PacketEvents.getPluginName()))
            return;

        PacketEvents.getAPI().terminate();
    }

    @Override
    public boolean isHookLoaded() {
        return isPluginPresent(Hook.PacketEvents.getPluginName()) && PacketEvents.getAPI().isLoaded();
    }
}
