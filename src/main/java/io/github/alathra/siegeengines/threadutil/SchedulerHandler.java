package io.github.alathra.siegeengines.threadutil;

import io.github.alathra.siegeengines.SiegeEngines;
import io.github.alathra.siegeengines.Reloadable;
import io.github.milkdrinkers.threadutil.PlatformBukkit;
import io.github.milkdrinkers.threadutil.Scheduler;

import java.time.Duration;

/**
 * A wrapper handler class for handling thread-util lifecycle.
 */
public class SchedulerHandler implements Reloadable {
    @Override
    public void onLoad(SiegeEngines plugin) {
        Scheduler.init(new PlatformBukkit(plugin)); // Initialize thread-util
        Scheduler.setErrorHandler(e -> plugin.getSLF4JLogger().error("[Scheduler]: {}", e.getMessage()));
    }

    @Override
    public void onEnable(SiegeEngines plugin) {

    }

    @Override
    public void onDisable(SiegeEngines plugin) {
        if (Scheduler.isInitialized())
            Scheduler.shutdown(Duration.ofSeconds(60));
    }
}
