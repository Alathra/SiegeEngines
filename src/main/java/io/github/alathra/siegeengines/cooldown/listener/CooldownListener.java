package io.github.alathra.siegeengines.cooldown.listener;

import io.github.alathra.siegeengines.SiegeEngines;
import io.github.alathra.siegeengines.cooldown.Cooldowns;
import io.github.milkdrinkers.threadutil.Scheduler;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

@SuppressWarnings({"unused", "FieldCanBeLocal", "CodeBlock2Expr"})
class CooldownListener implements Listener {
    private final SiegeEngines plugin;

    public CooldownListener(SiegeEngines plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Scheduler.async(() -> {
                //Queries.Cooldown.load(e.getPlayer()).forEach((cooldownType, instant) -> {
                    //Cooldowns.set(e.getPlayer(), cooldownType, instant);
                //});
            })
            .execute();

    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        Scheduler.async(() -> {
                //Queries.Cooldown.save(e.getPlayer());
                Cooldowns.removeAll(e.getPlayer());
            })
            .execute();
    }
}
