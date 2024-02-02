package com.github.alathra.siegeengines.listeners;

import java.util.UUID;
import java.util.ArrayList;
import java.util.List;

import com.github.alathra.siegeengines.SiegeEngines;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import com.github.alathra.siegeengines.SiegeEngine;
import com.github.alathra.siegeengines.config.Config;

public class RotationHandler implements Listener {

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        NamespacedKey key = new NamespacedKey(SiegeEngines.getInstance(), "siege_engines");
        if (SiegeEngines.siegeEngineEntitiesPerPlayer.containsKey(player.getUniqueId())) {
            ItemStack itemInHand = player.getInventory().getItemInMainHand();
            if (itemInHand != null) {
                final List<Entity> list = new ArrayList<>(SiegeEngines.siegeEngineEntitiesPerPlayer.get(player.getUniqueId()));
                final List<Entity> concurrentFix = new ArrayList<>(list);
                for (Entity ent : list) {
                    if (ent != null) {
                        SiegeEngine equipment = SiegeEngines.activeSiegeEngines.get(ent.getUniqueId());
                        if (equipment == null) continue;
                        final LivingEntity living = (LivingEntity) ent;
                        if (Config.disabledWorlds.contains(ent.getWorld())) {
                            living.setHealth(0.0d);
                            PlayerHandler.siegeEngineEntityDied(ent);
                            continue;
                        }
                        if (!(ent.getLocation().getWorld().equals(player.getLocation().getWorld()))) {
                            PlayerHandler.releasePlayerSiegeEngine(player, ent);
                            continue;
                        }
                        if (ent.isDead()) {
                            PlayerHandler.releasePlayerSiegeEngine(player, ent);
                            continue;
                        }
                        if (!ent.isValid()) {
                            PlayerHandler.releasePlayerSiegeEngine(player, ent);
                            continue;
                        }
                        final double distance = player.getLocation().distance(ent.getLocation());
                        if (distance >= Config.controlDistance) {
                            PlayerHandler.releasePlayerSiegeEngine(player, ent);
                            continue;
                        }
                        if (itemInHand.getType() != Config.controlItem) {
                            continue;
                        }
                        if (distance <= Config.rotateDistance) {
                            Location loc = ent.getLocation();

                            if (equipment.isRotateSideways()) {
                                Location direction = player.getLocation().add(player.getLocation().getDirection().multiply(50));

                                Vector dirBetweenLocations = direction.toVector().subtract(loc.toVector());
                                if (ent.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
                                    Entity base = Bukkit.getEntity(UUID.fromString(ent.getPersistentDataContainer().get(key, PersistentDataType.STRING)));
                                    Location baseloc = base.getLocation();
                                    baseloc.setDirection(dirBetweenLocations);
                                    base.teleport(baseloc,io.papermc.paper.entity.TeleportFlag.EntityState.values());
                                }
                                loc.setDirection(dirBetweenLocations);
                            }


                            ArmorStand stand = (ArmorStand) living;

                            if (equipment.isRotateUpDown()) {
                                loc.setPitch(player.getLocation().getPitch());
                                if (loc.getPitch() < -85) {
                                    loc.setPitch(-85);
                                }
                                if (loc.getPitch() > 85) {
                                    loc.setPitch(85);
                                }
                                if (equipment.isRotateStandHead()) {
                                    stand.setHeadPose(new EulerAngle(loc.getDirection().getY() * (-1), 0, 0));
                                }
                            }


                            living.teleport(loc,io.papermc.paper.entity.TeleportFlag.EntityState.values());
                            equipment.ShowFireLocation(player);
                        }
                    }
                }
            }
        }
    }

    public Vector getDirectionBetweenLocations(Location Start, Location End) {
        Vector from = Start.toVector();
        Vector to = End.toVector();
        return to.subtract(from);
    }
}
