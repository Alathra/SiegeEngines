package com.github.alathra.siegeengines.listeners;

import com.github.alathra.siegeengines.SiegeEnginesDamageSource;
import com.github.alathra.siegeengines.SiegeEnginesLogger;
import com.github.alathra.siegeengines.config.Config;
import com.github.alathra.siegeengines.data.SiegeEnginesData;
import com.github.alathra.siegeengines.projectile.ExplosiveProjectile;
import com.github.alathra.siegeengines.util.GeneralUtil;
import com.github.alathra.siegeengines.util.SiegeEnginesUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;

import java.util.List;

public class SiegeEngineDamagedListener implements Listener {
    @EventHandler(priority = EventPriority.HIGH)
    public void onSiegeEngineHitByProjectile(ProjectileHitEvent event) {
        for (Entity entity : event.getEntity().getNearbyEntities(2, 2, 2)) {
            if (entity instanceof ArmorStand stand) {
                if (event.isCancelled()) return;
                if (SiegeEnginesUtil.isSiegeEngine(stand, false)) {
                    event.setCancelled(true);
                    SiegeEnginesLogger.debug("ARROW DAMAGE CANCELLED? : " + Config.arrowDamageToggle);
                    if (Config.arrowDamageToggle) {
                        continue;
                    }
                    SiegeEnginesLogger.debug("HEALTH BEOFRE SHOT : " + stand.getHealth());
                    if (stand.getHealth() - 2 > 0) {
                        stand.setHealth(stand.getHealth() - 2);
                    } else {
                        EntityDeathEvent death = new EntityDeathEvent(stand, new SiegeEnginesDamageSource(stand, event.getHitEntity(), stand), SiegeEnginesData.items);
                        Bukkit.getServer().getPluginManager().callEvent(death);
                        if (!death.isCancelled()) stand.setHealth(0.0f);
                    }
                    SiegeEnginesLogger.debug("HEALTH AFTER SHOT : " + stand.getHealth());
                    if (stand.isDead()) {
                        PlayerHandler.siegeEngineEntityDied(stand);
                    }
                }
            }
        }
        if (SiegeEnginesData.projectiles.containsKey(event.getEntity().getUniqueId())) {
            ExplosiveProjectile proj = SiegeEnginesData.projectiles.get(event.getEntity().getUniqueId());
            Projectile snowball = event.getEntity();
            Entity player = (Entity) snowball.getShooter();
            if (player instanceof Player) {
                player.sendMessage("§eDistance to impact: "
                        + String.format("%.2f", player.getLocation().distance(snowball.getLocation())));
            }
            Location loc = snowball.getLocation();
            Entity tnt = event.getEntity().getWorld().spawnEntity(loc, EntityType.TNT);

            SiegeEnginesData.projectiles.remove(event.getEntity().getUniqueId());

            if (proj.placeBlocks) {
                TNTPrimed tntEnt = (TNTPrimed) tnt;
                tntEnt.setYield(0);
                tntEnt.setFuseTicks(0);
                if (event.getHitBlock() != null) {
                    List<Block> Blocks = GeneralUtil.getSphere(event.getHitBlock().getLocation(), (int) proj.explodePower);
                    for (int i = 0; i < proj.blocksToPlaceAmount; i++) {
                        Block replace = (Block) GeneralUtil.getRandomElement(Blocks);
                        replace.setType(proj.blockToPlace);
                    }
                }
            } else {
                TNTPrimed tntEnt = (TNTPrimed) tnt;
                tntEnt.setYield(proj.explodePower);
                tntEnt.setFuseTicks(0);
            }
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onSiegeEngineDamageByEntity(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof ArmorStand stand) {
            if (SiegeEnginesUtil.isSiegeEngine(stand, false)) return;
            if (event.isCancelled()) return;
            if (event.getDamager() instanceof Player) {
                PlayerHandler.releasePlayerSiegeEngine((Player) (event.getDamager()), event.getEntity());
            }
            SiegeEnginesLogger.debug("HEALTH BEOFRE HIT : " + stand.getHealth());
            if (stand.getHealth() - 2 > 0) {
                stand.setHealth(stand.getHealth() - 2);
            } else {
                EntityDeathEvent death = new EntityDeathEvent(stand, event.getDamageSource(), SiegeEnginesData.items);
                Bukkit.getServer().getPluginManager().callEvent(death);
                if (death.isCancelled()) return;
                stand.setHealth(0.0f);
                PlayerHandler.siegeEngineEntityDied(event.getEntity());
            }
            SiegeEnginesLogger.debug("HEALTH AFTER HIT : " + stand.getHealth());
        }
    }
}

