package com.github.alathra.siegeengines;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import com.github.alathra.siegeengines.projectile.GunnersProjectile;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

//import GunnersProjectiles.GunnersProjectile;
//import GunnersProjectiles.ExplosiveProjectile;
//import me.libraryaddict.disguise.DisguiseAPI;
//import me.libraryaddict.disguise.disguisetypes.DisguiseType;
//import me.libraryaddict.disguise.disguisetypes.MiscDisguise;

public class GunnerEquipment implements Cloneable {

    @Override
    public GunnerEquipment clone() throws CloneNotSupportedException {
        return (GunnerEquipment) super.clone();
    }

    public Boolean Enabled = true;
    public UUID EntityId;
    public Entity Entity;
    public Integer shotAmount = 1;

    public HashMap<ItemStack, GunnersProjectile> Projectiles = new HashMap<ItemStack, GunnersProjectile>();

    public String WorldName;
    public String EquipmentName = "Peckle Gun";
    public int XOffset = 7;
    public int YOffset = 0;
    public int PitchOffset = 0;
    public int MaxFuel = 5;
    public double PlacementOffsetY = 0;
    public float VelocityPerFuel = 1.75f;
    public ItemStack FuelMaterial = new ItemStack(Material.GUNPOWDER);
    public long NextShotTime = System.currentTimeMillis();

    public int MillisecondsBetweenFiringStages;
    public int MillisecondsBetweenReloadingStages;

    public int MillisecondsToLoad;
    public int ModelNumberToFireAt;
    public Boolean CycleThroughModelsBeforeFiring = false;
    public Boolean RotateSideways = false;
    public Boolean RotateUpDown = true;
    public Boolean RotateStandHead = true;
    public Boolean HasFired = false;
    public Boolean HasReloaded = false;
    public Boolean AllowInvisibleStand = false;
    public Boolean HaseBaseStand = false;
    public double BaseStandOffset = 0;
    public int BaseStandModelNumber = 147;
    public int TaskNumber;

    public int ReadyModelNumber;

    public int NextModelNumber = 0;

    public List<Integer> FiringModelNumbers = new ArrayList<Integer>();

    public EquipmentMagazine AmmoHolder = new EquipmentMagazine();

    public Boolean isLoaded() {
        if (this.MaxFuel <= 0 && AmmoHolder.LoadedProjectile >= 1)
            return true;
        return (AmmoHolder.LoadedFuel > 0 && AmmoHolder.LoadedProjectile >= 1);
    }

    public Boolean CanLoadFuel() {
        return AmmoHolder.LoadedFuel < MaxFuel;
    }

    public Boolean LoadFuel(Entity player) {
        ItemStack fuelItem = this.FuelMaterial;
        if (this.AmmoHolder.LoadedFuel == this.MaxFuel) {
            return true;
        }
        if (!(player instanceof Player)) {
            this.AmmoHolder.LoadedFuel = this.MaxFuel;
            return true;
        }
        if (((Player) player).getInventory().containsAtLeast(fuelItem, 1) || fuelItem.getType() == Material.AIR) {
            if (CanLoadFuel()) {
                this.AmmoHolder.LoadedFuel += 1;
                SaveState();
                ((Player) player).getInventory().removeItem(fuelItem);
                ((Player) player).sendMessage("§eLoaded " + this.AmmoHolder.LoadedFuel + "/" + this.MaxFuel);
                return true;
            } else {
                return false;
            }
        }

        return false;

    }

    public Boolean LoadProjectile(Entity player, ItemStack itemInHand) {
        if (itemInHand.getAmount() <= 0) {
            return false;
        }
        if (!(player instanceof Player)) {
            this.AmmoHolder.LoadedProjectile = 1;
            this.AmmoHolder.LoadedFuel = this.MaxFuel;
            this.AmmoHolder.MaterialName = itemInHand;
            return true;
        }
        if (this.Projectiles.containsKey(itemInHand.getType()) && this.AmmoHolder.LoadedProjectile == 0) {
            this.AmmoHolder.LoadedProjectile = 1;
            this.AmmoHolder.MaterialName = itemInHand;
            ((Player) player).sendMessage("§eAdding Ammo to Weapon");
            itemInHand.setAmount(itemInHand.getAmount() - 1);
            return true;
        }
        return false;
    }

    public void SaveState() {

    }


    public Location GetFireLocation(LivingEntity living) {
        Location loc = living.getEyeLocation();
        Vector direction = living.getLocation().getDirection().multiply(XOffset);
        loc.add(direction);
        if (this.YOffset > 0) {
            loc.setY(loc.getY() + this.YOffset);
        } else {
            loc.setY(loc.getY() - this.YOffset);
        }
        loc.setPitch(loc.getPitch() + this.PitchOffset);
        return loc;
    }

    public void ShowFireLocation(Entity player) {
        LivingEntity living = (LivingEntity) this.Entity;
        if (player instanceof Player) {
            if (living != null) {
                Location origin = this.GetFireLocation(living);
                Vector direction = origin.getDirection();
                direction.multiply(5 /* the range */);
                Location destination = origin.clone().add(direction);

                direction.normalize();
                for (int i = 0; i < 3 /* range */; i += 1) {
                    Location spawn = origin.add(direction);
                    ((Player) player).spawnParticle(Particle.CLOUD, spawn, 1, 0, 0, 0, 0);
                }
            }
        }
    }

    public void Fire(Entity player, float delay, Integer amount) {
        if (amount == null || amount == 0)
            amount = this.shotAmount;
        this.shotAmount = amount;
        if (System.currentTimeMillis() < this.NextShotTime) {
            return;
        }
        if (this.AmmoHolder.LoadedFuel <= 0)
            this.AmmoHolder.LoadedFuel = 3;
        float loadedFuel = this.AmmoHolder.LoadedFuel;
        ItemStack LoadedProjectile = this.AmmoHolder.MaterialName;


        LivingEntity living = (LivingEntity) Entity;
        if (living == null || living.getEquipment() == null || living.getEquipment().getHelmet() == null || living.getEquipment().getHelmet().getItemMeta() == null) {
            return;
        }

        if (living.getEquipment().getHelmet().getItemMeta().getCustomModelData() != this.ReadyModelNumber) {
            if (player instanceof Player) {
                ((Player) player).sendMessage("§eCannot fire yet!");
            }
            return;
        }
        this.AmmoHolder.LoadedFuel = 0;
        this.AmmoHolder.LoadedProjectile = 0;
        this.AmmoHolder.MaterialName = new ItemStack(Material.AIR, 1);

        this.WorldName = Entity.getWorld().getName();
        this.NextShotTime = System.currentTimeMillis() + 1000;
        for (int i = 0; i <= this.shotAmount /* range */; i += 1) {
            if (living == null || living.isDead()) {
                return;
            }
            if (this.CycleThroughModelsBeforeFiring) {

                this.TaskNumber = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(SiegeEngines.plugin, () -> {
                    if (living == null || living.isDead()) {
                        Bukkit.getServer().getScheduler().cancelTask(this.TaskNumber);
                        return;
                    }

                    if (this.HasFired) {
                        Bukkit.getServer().getScheduler().cancelTask(this.TaskNumber);
                        this.TaskNumber = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(SiegeEngines.plugin, () -> {
                            if (living == null || living.isDead()) {
                                Bukkit.getServer().getScheduler().cancelTask(this.TaskNumber);
                                return;
                            }
                            //	player.sendMessage("§etask");
                            if (this.HasReloaded) {
                                Bukkit.getServer().getScheduler().cancelTask(this.TaskNumber);
                                this.HasReloaded = false;
                                this.HasFired = false;
                                this.NextModelNumber = 0;
                                SiegeEngines.UpdateEntityIdModel(this.Entity, this.ReadyModelNumber, this.WorldName);
                                this.TaskNumber = 0;
                            } else {
                                //firing stages
                                if (this.NextModelNumber - 1 <= this.FiringModelNumbers.size() && this.NextModelNumber - 1 >= 0) {
                                    int modelData = this.FiringModelNumbers.get(this.NextModelNumber - 1);
                                    //	player.sendMessage("§e" + modelData);
                                    SiegeEngines.UpdateEntityIdModel(this.Entity, modelData, this.WorldName);
                                    this.NextModelNumber -= 1;

                                } else {
                                    //	plugin.getLogger().log(Level.INFO, "its reloaded");
                                    this.HasReloaded = true;
                                }
                            }
                        }, 0, this.MillisecondsBetweenReloadingStages);

                    } else {
                        //firing stages
                        if (this.NextModelNumber < this.FiringModelNumbers.size()) {

                            int modelData = this.FiringModelNumbers.get(this.NextModelNumber);
                            //	player.sendMessage("§e" + modelData);
                            SiegeEngines.UpdateEntityIdModel(this.Entity, modelData, this.WorldName);
                            if (modelData == this.ModelNumberToFireAt) {
                                //	player.sendMessage("§efiring" + modelData);
                                Projectiles.get(LoadedProjectile).Shoot(player, this.Entity, this.GetFireLocation(living), loadedFuel * this.VelocityPerFuel);
                            }
                            this.NextModelNumber += 1;

                        } else {
                            this.HasFired = true;

                        }
                    }
                }, 0, this.MillisecondsBetweenFiringStages);
            } else {
                this.TaskNumber = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(SiegeEngines.plugin, () -> {
                    //player.sendMessage("§etask");
                    if (living == null || living.isDead()) {
                        Bukkit.getServer().getScheduler().cancelTask(this.TaskNumber);
                        return;
                    }

                    Location loc = living.getEyeLocation();
                    Vector direction = Entity.getLocation().getDirection().multiply(XOffset);

                    loc.add(direction);

                    this.NextModelNumber = 0;

                    Projectiles.get(LoadedProjectile).Shoot(player, this.Entity, this.GetFireLocation(living), loadedFuel * this.VelocityPerFuel);
                }, (long) delay);

            }
        }
    }

}
