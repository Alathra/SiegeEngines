package com.github.alathra.siegeengines.listeners;


import java.util.HashSet;
import java.util.EnumSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.Random;
import java.util.UUID;

import com.github.alathra.siegeengines.config.Config;
import com.github.alathra.siegeengines.EquipmentMagazine;
import com.github.alathra.siegeengines.SiegeEngines;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.block.Sign;
import org.bukkit.block.TileState;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.ArmorStand.LockType;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.SignChangeEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.EulerAngle;

import com.github.alathra.siegeengines.SiegeEquipment;
import com.github.alathra.siegeengines.projectile.ExplosiveProjectile;

//import com.palmergames.bukkit.towny.TownyAPI;
//import com.palmergames.bukkit.towny.event.actions.TownyDestroyEvent;
//import com.palmergames.bukkit.towny.object.TownBlock;

//import GunnersProjectiles.ExplosiveProjectile;

public class ClickHandler implements Listener {

    public float MinDelay = 5;

    public static HashMap<UUID, ExplosiveProjectile> projectiles = new HashMap<UUID, ExplosiveProjectile>();
    public static EnumSet<Material> fluidMaterials = EnumSet.of(Material.WATER, Material.LAVA, Material.BUBBLE_COLUMN, Material.SEAGRASS, Material.TALL_SEAGRASS, Material.KELP, Material.KELP_PLANT, Material.SEA_PICKLE);

    @EventHandler
    public void onExplode(EntityExplodeEvent e) {
        for (Entity entity : e.getEntity().getNearbyEntities(e.getYield() + 1, e.getYield() + 1, e.getYield() + 1)) {
            if (entity instanceof ArmorStand) {
                ArmorStand stand = (ArmorStand) entity;
				/*if (SiegeEngines.towny != null) {
					TownBlock block = TownyAPI.getInstance().getTownBlock(stand.getLocation());
					if (block != null) {
						if (block.hasTown()) {
							if (!block.getTownBlockOwner().getPermissions().explosion) {
								return;
							}
						}
					}
				}*/
                stand.eject();
                stand.remove();
            }
        }
    }

    @EventHandler
    public void onHit(ProjectileHitEvent event) {
        if ((event.getEntity() instanceof Snowball) && projectiles.containsKey(event.getEntity().getUniqueId())) {
            ExplosiveProjectile proj = projectiles.get(event.getEntity().getUniqueId());
            Entity snowball = event.getEntity();
            Snowball ball = (Snowball) snowball;
            Entity player = (Entity) ball.getShooter();
            if (player instanceof Player) {
                player.sendMessage("§eDistance to impact: " + String.format("%.2f", player.getLocation().distance(ball.getLocation())));
            }
            Location loc = snowball.getLocation();
            World world = event.getEntity().getWorld();
            //	world.createExplosion(loc, proj.Radius, proj.DoFire);
            Entity tnt = event.getEntity().getWorld().spawnEntity(loc, EntityType.PRIMED_TNT);

            projectiles.remove(event.getEntity().getUniqueId());

            if (proj.PlaceBlocks) {
                TNTPrimed tntEnt = (TNTPrimed) tnt;
                tntEnt.setYield(0);
                tntEnt.setFuseTicks(0);
				/*if (SiegeEngines.towny != null) {
					TownBlock block = TownyAPI.getInstance().getTownBlock(loc);
					if (block != null) {
						if (block.hasTown()) {
							if (!block.getTownBlockOwner().getPermissions().explosion) {
								return;
							}
						}
					}
				}*/
                if (event.getHitBlock() != null) {
                    List<Block> Blocks = sphere(event.getHitBlock().getLocation(), (int) proj.ExplodePower);
                    for (int i = 0; i < proj.BlocksToPlaceAmount; i++) {
                        Block replace = getRandomElement(Blocks);
                        replace.setType(proj.BlockToPlace);
                    }
                }
            } else {
                TNTPrimed tntEnt = (TNTPrimed) tnt;
                tntEnt.setYield(proj.ExplodePower);
                tntEnt.setFuseTicks(0);
            }
        }
    }

    public ArrayList<Block> sphere(final Location center, final int radius) {
        ArrayList<Block> sphere = new ArrayList<Block>();
        for (int Y = -radius; Y < radius; Y++) {
            for (int X = -radius; X < radius; X++) {
                for (int Z = -radius; Z < radius; Z++) {
                    if (Math.sqrt((X * X) + (Y * Y) + (Z * Z)) <= radius) {
                        final Block block = center.getWorld().getBlockAt(X + center.getBlockX(), Y + center.getBlockY(), Z + center.getBlockZ());
                        if (block.getType() == Material.AIR) {
                            sphere.add(block);
                        }
                    }
                }
            }
        }
        return sphere;
    }

    public Block getRandomElement(List<Block> list) {
        Random rand = new Random();
        return list.get(rand.nextInt(list.size()));
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void BlockPlaceEvent(org.bukkit.event.block.BlockPlaceEvent event) {
        Player thePlayer = event.getPlayer();
        if (event.isCancelled())
            return;
        Material replaced = event.getBlockReplacedState().getType();
        if (fluidMaterials.contains(replaced)) {
            thePlayer.sendMessage("§eGunner Equipment cannot be spawned in Fluid Blocks.");
            event.setCancelled(true);
            return;
        }
        if (event.getPlayer().getInventory().getItemInMainHand().getType() == Material.CARVED_PUMPKIN) {
            ItemStack item = event.getPlayer().getInventory().getItemInMainHand();
            if (item.getItemMeta() != null && item.getItemMeta().hasCustomModelData()) {
                int customModel = item.getItemMeta().getCustomModelData();
                Boolean created = SiegeEngines.CreateCannon(thePlayer, customModel, event.getBlockAgainst().getLocation());
                if (created) {
                    item.setAmount(item.getAmount() - 1);
                    thePlayer.getInventory().setItemInMainHand(item);
                    thePlayer.sendMessage("§eGunner Equipment spawned!");
                } else {
                    thePlayer.sendMessage("§eGunner Equipment could not be spawned, is it enabled?");
                }
                event.setCancelled(true);
            }
        }
    }


    public void Shoot(Entity player, long delay) {
        float actualDelay = delay;
        Boolean FirstShot = true;
        int counter = 0;
        if (SiegeEngines.TrackedStands.get(player.getUniqueId()) == null) {
            for (Entity ent : player.getNearbyEntities(5, 5, 5)) {
                if (ent instanceof ArmorStand) {
                    TakeControl(player, ent);
                }
            }
        }
        if (SiegeEngines.TrackedStands.get(player.getUniqueId()) == null)
            return;
        List<Entity> entities = new ArrayList<>(SiegeEngines.TrackedStands.get(player.getUniqueId()));
        for (Entity ent : entities) {
            if (ent == null || ent.isDead()) {
                SiegeEngines.TrackedStands.get(player.getUniqueId()).remove(ent);
                continue;
            }
            if (((LivingEntity) ent).getEquipment().getHelmet().getType() != Material.CARVED_PUMPKIN) {
                SiegeEngines.TrackedStands.get(player.getUniqueId()).remove(ent);
                continue;
            }
            double distance = player.getLocation().distance(ent.getLocation());
            if (distance >= 32) {
                SiegeEngines.TrackedStands.get(player.getUniqueId()).remove(ent);
                continue;
            }
            SiegeEquipment siege = SiegeEngines.equipment.get(ent.getUniqueId());
            if (counter > 4)
                return;
            if (ent == null || ent.isDead()) {
                SiegeEngines.TrackedStands.get(player.getUniqueId()).remove(ent);
                continue;
            }
            if (siege.isLoaded()) {
                if (player instanceof Player) {
                    siege.Fire(player, actualDelay, 1);
                    actualDelay += delay;
                } else {
                    siege.Fire(player, 15, 1);
                }
                counter++;
            } else {
                if (player instanceof Player) {
                }
                //player.sendMessage("§eAmmunition Not Loaded");
            }
            //	player.sendMessage(String.format("§e" +actualDelay));

        }
    }

    public static ItemStack[] updateContents(Inventory inv, ItemStack m, int toRemove) {
        ItemStack[] contents = inv.getStorageContents();
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] == null) continue;
            if (contents[i].isSimilar(m)) {
                int amountInInv = contents[i].getAmount();
                if (toRemove >= amountInInv) {
                    contents[i].setType(Material.AIR);
                    toRemove -= amountInInv;
                } else {
                    contents[i].setAmount(amountInInv - toRemove);
                    toRemove = 0;
                    break;
                }
            }
            if (toRemove <= 0) break;
        }
        return contents;
    }

	public static boolean hasItem(Inventory inv, ItemStack m) {
			//ItemStack[] contents = inv.getStorageContents();
			/*for(int i = 0; i < contents.length; i++) {
					if(contents[i] == null) continue;
					if(contents[i].isSimilar(m)) {
							return true;
					}
			}*/
            //m.setAmount(1);
            return inv.containsAtLeast(m,m.getAmount());
			//return false;
	}

    @EventHandler
    public void interact(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        ItemStack ItemInHand = event.getPlayer().getInventory().getItemInMainHand();
        if (ItemInHand == null) {
            return;
        }

        //		if (ItemInHand.getType() == Material.PAPER) {
        //			ItemMeta meta = ItemInHand.getItemMeta();
        //			if (meta.hasCustomModelData() && meta.getCustomModelData() == 505050505) {
        //				SiegeEngines.CreateCannon(player);
        //				ItemInHand.setAmount(ItemInHand.getAmount() - 1);
        //				return;
        //			}
        //		}


        if (event.getAction() == Action.RIGHT_CLICK_AIR) {
            if (ItemInHand.getType() != Config.controlItem) {
                return;
            }
            if (!player.isSneaking()) {
                List<Entity> entities = SiegeEngines.TrackedStands.get(player.getUniqueId());
                if (entities == null) {
                    return;
                }
                for (Entity entity : entities) {
                    
                    if (SiegeEngines.equipment.containsKey(entity.getUniqueId())) {
                        SiegeEquipment equip = SiegeEngines.equipment.get(entity.getUniqueId());
                        if (equip != null && equip.Enabled && !(entity.isDead())) {
                            equip.Fire(player,10f,1);
                        }
                    }
                }
                return;
            }
            if (SiegeEngines.TrackedStands.containsKey(player.getUniqueId())) {
                List<Entity> entities = SiegeEngines.TrackedStands.get(player.getUniqueId());
                if (entities == null) {
                    return;
                }
                player.sendMessage("§eReloading Ammunition");
                boolean foundAmmo = false;
                for (Entity entity : entities) {
                    foundAmmo = false;
                    if ((entity.isDead())) continue;
                    if (SiegeEngines.equipment.containsKey(entity.getUniqueId())) {
                        SiegeEquipment equip = SiegeEngines.equipment.get(entity.getUniqueId());
                        if (equip == null || !equip.Enabled) {
                            continue;
                        }
                        if (entity.getLocation().getBlock().getType() == Material.BARREL || entity.getLocation().getBlock().getType() == Material.CHEST) {
                            Block inv = entity.getLocation().getBlock();
                            Container state = ((Container) inv.getState());
                            for (ItemStack stack : equip.Projectiles.keySet()) {
                                if (foundAmmo)
                                    continue;
                                if (!hasItem((Inventory)state.getInventory(),stack))
                                    continue;
                                state.getInventory().removeItem(stack);//setStorageContents(updateContents(state.getInventory(),stack,1));
                                //state.update(true,true);
                                equip.AmmoHolder.LoadedProjectile = 1;
                                equip.AmmoHolder.MaterialName = stack;
                                foundAmmo = true;
                            }
                            //state.update(true,true);
                        }
                        if (foundAmmo)
                            break;
                        if (entity.getLocation().getBlock().getRelative(0, -1, 0).getType() == Material.BARREL || entity.getLocation().getBlock().getRelative(0, -1, 0).getType() == Material.CHEST) {
                            Block inv = entity.getLocation().getBlock().getRelative(0, -1, 0);
                            Container state = ((Container) inv.getState());
                            for (ItemStack stack : equip.Projectiles.keySet()) {
                                if (foundAmmo)
                                    continue;
                                if (!hasItem((Inventory)state.getInventory(),stack))
                                    continue;
                                state.getInventory().removeItem(stack);//setStorageContents(updateContents(state.getInventory(),stack,1));
                                //state.update(true,true);
                                equip.AmmoHolder.LoadedProjectile = 1;
                                equip.AmmoHolder.MaterialName = stack;
                                foundAmmo = true;
                            }
                            //state.update(true,true);
                        }
                        if (foundAmmo)
                            break;
                        if (entity.getLocation().getBlock().getRelative(0, 1, 0).getType() == Material.BARREL || entity.getLocation().getBlock().getRelative(0, 1, 0).getType() == Material.CHEST) {
                            Block inv = entity.getLocation().getBlock().getRelative(0, 1, 0);
                            Container state = ((Container) inv.getState());
                            for (ItemStack stack : equip.Projectiles.keySet()) {
                                if (foundAmmo)
                                    continue;
                                if (!hasItem((Inventory)state.getInventory(),stack))
                                    continue;
                                state.getInventory().removeItem(stack);//setStorageContents(updateContents(state.getInventory(),stack,1));
                                //state.update(true,true);
                                equip.AmmoHolder.LoadedProjectile = 1;
                                equip.AmmoHolder.MaterialName = stack;
                                foundAmmo = true;
                            }
                            //state.update(true,true);
                        }
                        if (foundAmmo)
                            break;
                        if (!player.isSneaking())
                            break;
                        if (!foundAmmo) {
                            for (ItemStack inventoryItem : event.getPlayer().getInventory().getContents()) {
                                for (ItemStack stack : equip.Projectiles.keySet()) {
                                    if (foundAmmo)
                                        continue;
                                    if (!hasItem(event.getPlayer().getInventory(),stack))
                                        continue;
                                    if (stack.isSimilar(inventoryItem) && equip.AmmoHolder.LoadedProjectile == 0) {
                                        equip.AmmoHolder.LoadedProjectile = 1;
                                        equip.AmmoHolder.MaterialName = stack;
                                        inventoryItem.setAmount(inventoryItem.getAmount() - 1);
                                        foundAmmo = true;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                if (!foundAmmo) {
                    event.getPlayer().sendMessage("§eCould not load Ammunition.");
                }
                player.sendMessage("§eReloading Propellant");
                for (Entity entity : entities) {
                    foundAmmo = false;
                    if ((entity.isDead())) continue;
                    if (SiegeEngines.equipment.containsKey(entity.getUniqueId())) {
                        SiegeEquipment equip = SiegeEngines.equipment.get(entity.getUniqueId());
                        if (equip == null || !equip.Enabled) {
                            continue;
                        }
                        ItemStack stack = equip.FuelMaterial;
                        if (entity.getLocation().getBlock().getType() == Material.BARREL || entity.getLocation().getBlock().getType() == Material.CHEST) {
                            Block inv = entity.getLocation().getBlock();
                            Container state = ((Container) inv.getState());
                            if (foundAmmo)
                                continue;
                            if (!hasItem((Inventory)state.getInventory(),stack))
                                continue;
                            if (!equip.CanLoadFuel())
                                continue;
                            state.getInventory().removeItem(stack);//setStorageContents(updateContents(state.getInventory(),stack,1));
                            //state.update(true,true);
                            equip.AmmoHolder.LoadedFuel += 1;
                            foundAmmo = true;
                            //state.update(true,true);
                        }
                        if (foundAmmo)
                            break;
                        if (entity.getLocation().getBlock().getRelative(0, -1, 0).getType() == Material.BARREL || entity.getLocation().getBlock().getRelative(0, -1, 0).getType() == Material.CHEST) {
                            Block inv = entity.getLocation().getBlock().getRelative(0, -1, 0);;
                            Container state = ((Container) inv.getState());
                            if (foundAmmo)
                                continue;
                            if (!hasItem((Inventory)state.getInventory(),stack))
                                continue;
                            state.getInventory().removeItem(stack);//setStorageContents(updateContents(state.getInventory(),stack,1));
                            //state.update(true,true);
                            if (!equip.CanLoadFuel())
                                continue;
                            equip.AmmoHolder.LoadedFuel += 1;
                            foundAmmo = true;
                            //state.update(true,true);
                        }
                        if (foundAmmo)
                            break;
                        if (entity.getLocation().getBlock().getRelative(0, 1, 0).getType() == Material.BARREL || entity.getLocation().getBlock().getRelative(0, 1, 0).getType() == Material.CHEST) {
                            Block inv = entity.getLocation().getBlock().getRelative(0, 1, 0);
                            Container state = ((Container) inv.getState());
                            if (foundAmmo)
                                continue;
                            if (!hasItem((Inventory)state.getInventory(),stack))
                                continue;
                            if (!equip.CanLoadFuel())
                                continue;
                            state.getInventory().removeItem(stack);//setStorageContents(updateContents(state.getInventory(),stack,1));
                            //state.update(true,true);
                            equip.AmmoHolder.LoadedFuel += 1;
                            foundAmmo = true;
                            //state.update(true,true);
                        }
                        if (foundAmmo)
                            break;
                        if (!player.isSneaking())
                            break;
                        if (!foundAmmo) {
                            if (hasItem((Inventory)event.getPlayer().getInventory(),stack)) {
                                if (foundAmmo)
                                    break;
                                if (!equip.CanLoadFuel())
                                    continue;
                                equip.AmmoHolder.LoadedFuel += 1;
                                stack.setAmount(1);
                                event.getPlayer().getInventory().removeItem(stack);
                                foundAmmo = true;
                                break;
                            }
                        }
                        if (!foundAmmo) {
                            event.getPlayer().sendMessage("§eCould not load Propellant.");
                        }
                    }
                }
                return;
            }

        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onSignChange(SignChangeEvent event) {
        String topline = event.getLine(0);
        if (topline == null) topline = "";
        Player player = event.getPlayer();
        String toplinetrimmed = topline.trim();
        if (toplinetrimmed.equalsIgnoreCase("[Cannon]")) {
            SaveCannons(player, event.getBlock());
        }

    }

    @EventHandler
    public void damage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof ArmorStand) {
            if (event.getDamager() instanceof Projectile) {
                event.setDamage(0);
                event.setCancelled(true);
            }
        }
    }


    public static void TakeControl(Entity player, Entity entity) {
        LivingEntity living = (LivingEntity) entity;
        if (SiegeEngines.TrackedStands.containsKey(player.getUniqueId())) {
            List<Entity> entities = SiegeEngines.TrackedStands.get(player.getUniqueId());
            if (entities.contains(entity)) {
                return;
            }
        }

        if (living.getEquipment().getHelmet() != null && living.getEquipment().getHelmet().getType() == Material.CARVED_PUMPKIN) {
            if (living.getEquipment() == null || living.getEquipment().getHelmet() == null || living.getEquipment().getHelmet().getItemMeta() == null) {
                return;
            }

            ArmorStand stand = (ArmorStand) entity;
            SiegeEquipment equip;
            stand.addEquipmentLock(EquipmentSlot.HEAD, LockType.REMOVING_OR_CHANGING);
            stand.addEquipmentLock(EquipmentSlot.LEGS, LockType.ADDING_OR_CHANGING);
            stand.addEquipmentLock(EquipmentSlot.CHEST, LockType.ADDING_OR_CHANGING);
            stand.addEquipmentLock(EquipmentSlot.FEET, LockType.ADDING_OR_CHANGING);
            stand.setBasePlate(false);

            if (SiegeEngines.equipment.containsKey(entity.getUniqueId())) {
                equip = SiegeEngines.equipment.get(entity.getUniqueId());
                if (equip == null || !equip.Enabled) {
                    return;
                }
            } else {
                equip = SiegeEngines.CreateClone(living.getEquipment().getHelmet().getItemMeta().getCustomModelData());
                if (equip == null || !equip.Enabled) {
                    return;
                }
                equip.AmmoHolder = new EquipmentMagazine();
                equip.Entity = entity;
                equip.EntityId = entity.getUniqueId();
            }
            stand.addEquipmentLock(EquipmentSlot.HEAD, LockType.REMOVING_OR_CHANGING);
            stand.addEquipmentLock(EquipmentSlot.LEGS, LockType.ADDING_OR_CHANGING);
            stand.addEquipmentLock(EquipmentSlot.CHEST, LockType.ADDING_OR_CHANGING);
            stand.addEquipmentLock(EquipmentSlot.FEET, LockType.ADDING_OR_CHANGING);
            stand.setBasePlate(false);
            if (SiegeEngines.TrackedStands.containsKey(player.getUniqueId())) {
                List<Entity> entities = SiegeEngines.TrackedStands.get(player.getUniqueId());
                entities.add(entity);
                SiegeEngines.TrackedStands.put(player.getUniqueId(), entities);
            } else {
                List<Entity> newList = new ArrayList<Entity>();
                newList.add(entity);
                SiegeEngines.TrackedStands.put(player.getUniqueId(), newList);
            }
            SiegeEngines.equipment.put(entity.getUniqueId(), equip);
            //player.sendMessage("§eNow controlling the equipment.");
        }
    }

    public void SaveCannons(Player player, Block block) {
        List<String> Ids = new ArrayList<String>();
        if (!SiegeEngines.TrackedStands.containsKey(player.getUniqueId())) {
            return;
        }
        for (Entity ent : SiegeEngines.TrackedStands.get(player.getUniqueId())) {
            if (ent.isDead()) {
                continue;
            }
            Ids.add(ent.getUniqueId().toString());
        }
        TileState state = ((TileState) block.getState());
        NamespacedKey key = new NamespacedKey(SiegeEngines.plugin, "cannons");

        for (Entity ent : SiegeEngines.TrackedStands.get(player.getUniqueId())) {
            if (ent.isDead()) {
                continue;
            }
            DoAimUp(ent, 0, player);
        }
    }

    public void DoAimUp(Entity ent, float amount, Entity player) {
        Location loc = ent.getLocation();
        ArmorStand stand = (ArmorStand) ent;
        //	player.sendMessage(String.format("" + loc.getPitch()));
        if (loc.getPitch() == -85 || loc.getPitch() - amount < -85) {
            return;
        }
        loc.setPitch((float) (loc.getPitch() - amount));
        SiegeEquipment equipment = SiegeEngines.equipment.get(ent.getUniqueId());
        if (equipment != null) {
            if (player instanceof Player)
                equipment.ShowFireLocation(player);
            if (equipment.RotateStandHead) {
                stand.setHeadPose(new EulerAngle(loc.getDirection().getY() * (-1), 0, 0));
            }
            ent.teleport(loc);
        }
    }

    public void DoAimDown(Entity ent, float amount, Entity player) {
        Location loc = ent.getLocation();
        ArmorStand stand = (ArmorStand) ent;
        //	player.sendMessage(String.format("" + loc.getPitch()));
        if (loc.getPitch() == 85 || loc.getPitch() - amount < 65) {
            return;
        }
        loc.setPitch((float) (loc.getPitch() - amount));
        SiegeEquipment equipment = SiegeEngines.equipment.get(ent.getUniqueId());
        if (equipment != null) {
            if (player instanceof Player)
                equipment.ShowFireLocation(player);
            if (equipment.RotateStandHead) {
                stand.setHeadPose(new EulerAngle(loc.getDirection().getY() * (-1), 0, 0));
            }
            ent.teleport(loc);
        }
    }

    public void FirstShotDoAimDown(Entity ent, float amount, Entity player) {
        Location loc = ent.getLocation();
        ArmorStand stand = (ArmorStand) ent;
        //	player.sendMessage(String.format("" + loc.getPitch()));
        if (loc.getPitch() == 85 || loc.getPitch() + amount > 85) {
            return;
        }
        SiegeEquipment equipment = SiegeEngines.equipment.get(ent.getUniqueId());
        if (equipment != null) {
            if (player instanceof Player) {
                equipment.ShowFireLocation((Player) player);
            }
            if (equipment.RotateStandHead) {
                stand.setHeadPose(new EulerAngle(loc.getDirection().getY() * (-1), 0, 0));

            }
        }
        loc.setPitch((float) (loc.getPitch() + amount));

        ent.teleport(loc);
    }

    NamespacedKey key = new NamespacedKey(SiegeEngines.plugin, "cannons");

    @EventHandler
    public void DeathEvent(EntityDeathEvent event) {
        Boolean removeStands = false;
        List<ItemStack> items = event.getDrops();
        if (event.getEntity() instanceof ArmorStand) {
            if (event.getEntity().getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
                Entity base = Bukkit.getEntity(UUID.fromString(event.getEntity().getPersistentDataContainer().get(key, PersistentDataType.STRING)));
                base.remove();
            }
            if (SiegeEngines.equipment.containsKey(event.getEntity().getUniqueId())) {
                removeStands = true;
            } else {
                for (ItemStack i : items) {
                    if (i.getType() == Material.CARVED_PUMPKIN && i.hasItemMeta() && i.getItemMeta().hasCustomModelData()) {
                        if (SiegeEngines.DefinedEquipment.containsKey(i.getItemMeta().getCustomModelData())) {
                            removeStands = true;
                            break;
                        }

                    }
                }
            }
            if (removeStands) {
                for (ItemStack i : items) {
                    if (i.getType() == Material.ARMOR_STAND) {
                        i.setAmount(0);
                        return;
                    }
                }
            }
        }
    }

    public void AimUp(Entity player, float amount) {

        for (Entity ent : SiegeEngines.TrackedStands.get(player.getUniqueId())) {
            if (ent.isDead()) {
                continue;
            }
            DoAimUp(ent, amount, player);
        }
    }

    public void AimDown(Entity player, float amount) {

        for (Entity ent : SiegeEngines.TrackedStands.get(player.getUniqueId())) {
            if (ent.isDead()) {
                continue;
            }
            DoAimDown(ent, amount, player);
        }
    }

    public void LoadCannonsWithPowder(Entity player) {
        for (Entity ent : SiegeEngines.TrackedStands.get(player.getUniqueId())) {
            if (ent.isDead()) {
                continue;
            }

            SiegeEquipment equipment = SiegeEngines.equipment.get(ent.getUniqueId());
            if (equipment != null) {
                equipment.LoadFuel(player);
            }
        }
    }

    public void LoadCannonsWithProjectile(Entity player, ItemStack projectile) {
        for (Entity ent : SiegeEngines.TrackedStands.get(player.getUniqueId())) {
            if (ent.isDead()) {
                continue;
            }
            SiegeEquipment equipment = SiegeEngines.equipment.get(ent.getUniqueId());
            if (equipment != null) {
                equipment.LoadProjectile(player, projectile);
            }
        }
    }

    @EventHandler
    public void onPlayerClickSign(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        if (event.getClickedBlock() != null && event.getClickedBlock().getType().toString().contains("SIGN")) {
            Sign sign = (Sign) event.getClickedBlock().getState();
            if (sign.getLine(0).equalsIgnoreCase("[Fire]") && event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                if (!SiegeEngines.TrackedStands.containsKey(player.getUniqueId())) {
                    return;
                }
                try {
                    Long delay = Long.parseLong(sign.getLine(1));
                    if (delay < 6) {
                        delay = 6l;
                    }
                    Shoot((Entity) player, delay);
                } catch (Exception e) {
                    Shoot((Entity) player, 6);
                }
                return;
            }
            if (sign.getLine(0).equalsIgnoreCase("[Load]")) {
                if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                    LoadCannonsWithPowder((Entity) player);
                    return;
                }
                if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    //load proj
                    ItemStack itemInHand = player.getInventory().getItemInMainHand();
                    if (itemInHand == null) {
                        return;
                    }


                    LoadCannonsWithProjectile((Entity) player, player.getInventory().getItemInMainHand());
                    return;
                }
            }

            if (sign.getLine(0).equalsIgnoreCase("[Aim]")) {
                if (!SiegeEngines.TrackedStands.containsKey(player.getUniqueId())) {
                    return;
                }
                float amount;
                try {
                    amount = Float.parseFloat(sign.getLine(1));
                    if (player.isSneaking()) {
                        AimDown((Entity) player, amount);
                    } else {
                        AimUp((Entity) player, amount);
                    }
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    player.sendMessage("§eCould not parse number on second line.");
                }

                return;
            }

            if (sign.getLine(0).equalsIgnoreCase("[Cannon]")) {
                if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
                    SiegeEngines.TrackedStands.remove(player.getUniqueId());
                    player.sendMessage("§eReleasing the equipment!");
                    return;
                }

                if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
                    if (player.isSneaking()) {
                        SaveCannons(player, event.getClickedBlock());
                        return;
                    }

                    NamespacedKey key = new NamespacedKey(SiegeEngines.plugin, "cannons");
                    TileState state = (TileState) sign.getBlock().getState();
                    SiegeEngines.TrackedStands.remove(player.getUniqueId());
                    List<UUID> temp = new ArrayList<UUID>();
                    if (!state.getPersistentDataContainer().has(key, PersistentDataType.STRING)) {
                        return;
                    }
                    String[] split = state.getPersistentDataContainer().get(key, PersistentDataType.STRING).replace("[", "").replace("]", "").split(",");
                    for (String s : split) {
                        temp.add(UUID.fromString(s.trim()));
                    }
                    for (UUID Id : temp) {
                        List<Entity> entities = new ArrayList<Entity>();
                        Entity ent = Bukkit.getEntity(Id);
                        if (ent != null) {
                            TakeControl(player, ent);
                        }
                    }
                    player.sendMessage("§eNow controlling nearby equipment.");
                    return;
                }
            }

        }
    }

    @EventHandler
    public void onEntityClick(PlayerInteractAtEntityEvent event) {

        Player player = event.getPlayer();
        ItemStack itemInHand = player.getInventory().getItemInMainHand();
        Entity entity = event.getRightClicked();
        if (entity == null) {
            return;
        }
        if (entity.getType() == EntityType.ARMOR_STAND) {
            if (itemInHand.getType() != Config.controlItem) {
                SiegeEngines.TrackedStands.get(player.getUniqueId()).remove(entity);
                player.sendMessage("§eThis Equipment is no longer commanded by you.");
                return;
            } else {
                TakeControl(player, entity);
            }
            if (SiegeEngines.equipment.containsKey(entity.getUniqueId())) {
                if (itemInHand.getType() == Material.CLOCK) {
                    if (player.isSneaking()) {
                        DoAimDown(entity, 1, player);
                    } else {
                        DoAimUp(entity, 1, player);
                    }
                    return;
                }

                SiegeEquipment equipment = SiegeEngines.equipment.get(entity.getUniqueId());
                event.setCancelled(true);

                if (itemInHand == null || itemInHand.getType() == Material.AIR) {
                    ArmorStand stand = (ArmorStand) entity;
                    if (stand.isInvisible()) {
                        stand.setInvisible(false);
                        player.sendMessage("§eEquipment is now breakable");
                    } else {
                        if (equipment.AllowInvisibleStand) {
                            stand.setInvisible(true);
                            player.sendMessage("§eEquipment is no longer breakable");
                        }
                    }
                    return;
                }
                if (itemInHand.isSimilar(equipment.FuelMaterial)) {
                    if (!equipment.LoadFuel(player)) {
                        player.sendMessage("§eCould not load Propellant.");
                        return;
                    }
                }
				/*if (itemInHand.isSimilar(new ItemStack(Material.FLINT))) {
					if (equipment.isLoaded()) {
						equipment.Fire((Entity) player, 6, 1);
					}
					else {
						player.sendMessage("§eEquipment is not loaded");
					}
					return;
				}*/
                for (ItemStack stack : equipment.Projectiles.keySet()) {
                    if (stack.isSimilar(itemInHand) && equipment.AmmoHolder.LoadedProjectile == 0) {
                        equipment.AmmoHolder.LoadedProjectile = 1;
                        equipment.AmmoHolder.MaterialName = stack;
                        player.sendMessage("§eAdding Ammunition to Weapon");
                        itemInHand.setAmount(itemInHand.getAmount() - 1);
                        return;
                    }

                }
                player.sendMessage("§eNow controlling this equipment.");
                return;
            }
        }
    }
}
