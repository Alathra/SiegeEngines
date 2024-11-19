package com.github.alathra.siegeengines;

import com.github.alathra.siegeengines.config.Config;
import com.github.alathra.siegeengines.projectile.EntityProjectile;
import com.github.alathra.siegeengines.projectile.FireworkProjectile;
import com.github.alathra.siegeengines.projectile.SiegeEngineProjectile;
import com.github.alathra.siegeengines.util.SiegeEnginesUtil;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.entity.ArmorStand.LockType;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;


public class SiegeEngine implements Cloneable {

    // Core variables
    private SiegeEngineType type;
    private Boolean enabled;
    private String id;
    private String itemName;
    private List<String> itemLore;
    private String worldName;
    private UUID entityId;
    private Entity entity;
    private int yOffset;
    private int xOffset;
    // private int pitchOffset; // Internal - Never Initialized and Defaulted to 0
    private int maxFuel;
    private double placementOffsetY;
    private float velocityPerFuel;
    private double baseHealth = 5.0d;
    // Firing variables
    private int shotAmount;
    private long nextShotTime; // Internal
    private int millisecondsBetweenFiringStages;
    private int millisecondsBetweenReloadingStages;
    // private int millisecondsToLoad; // Internal - Never Used
    private int modelNumberToFireAt;
    private boolean cycleThroughModelsWhileFiring;
    private boolean setModelNumberWhenFullyLoaded;
    private boolean rotateSideways;
    private boolean rotateUpDown;
    private boolean rotateStandHead;
    private boolean hasFired; // Internal
    private boolean hasReloaded; // Internal
    private int taskNumber;
    private List<Integer> firingModelNumbers;
    private int readyModelNumber;
    private int nextModelNumber; // Internal
    private int preFireModelNumber;
    private int preLoadModelNumber;
    private SiegeEngineAmmoHolder ammoHolder;

    // Optional variables
    private boolean allowInvisibleStand; // Internal
    private final boolean hasBaseStand; // Internal
    private final double baseStandOffset; // Internal
    private final int baseStandModelNumber; // Internal
    private boolean mountable = false;

    // Passed parameters
    private String engineName = "Unnammed SiegeEngine";
    private ItemStack fuelItem = new ItemStack(Material.GUNPOWDER);
    private HashMap<ItemStack, SiegeEngineProjectile> projectiles = new HashMap<>();

    private int customModelID = 150;

    @Override
    public SiegeEngine clone() throws CloneNotSupportedException {
        return (SiegeEngine) super.clone();
    }

    public SiegeEngine(@NotNull String name, @NotNull HashMap<ItemStack, SiegeEngineProjectile> projectiles, @NotNull ItemStack fuelItem, int customModelID) {

        // Default custom model data id if it is passes as null
        if (customModelID == 0) {
            customModelID = 150;
        }

        // Set Default values
        setType(SiegeEngineType.UNKNOWN);
        setItemName(ChatColor.translateAlternateColorCodes('&', "&eUnknown Siege Engine"));
        setItemLore(new ArrayList<String>());
        setXOffset(0);
        setYOffset(0);
        setMaxFuel(5);
        setVelocityPerFuel(1.0125f);
        id = name.replaceAll(" ", "_").toLowerCase();
        setRotateStandHead(true);
        setRotateSideways(true);
        setPlacementOffsetY(-1.125f);
        setReadyModelNumber(customModelID);
        setModelNumberToFireAt(customModelID);
        setPreFireModelNumber(customModelID);
        setPreLoadModelNumber(customModelID);
        setFiringModelNumbers(new ArrayList<Integer>());
        setAmmoHolder(new SiegeEngineAmmoHolder());
        //fuelItem = new ItemStack(Material.GUNPOWDER);
        setCycleThroughModelsWhileFiring(false);
        setSetModelNumberWhenFullyLoaded(false);
        allowInvisibleStand = false;
        setShotAmount(1);
        getAmmoHolder().setLoadedFuel(getMaxFuel());
        setEnabled(true);
        setRotateSideways(false);
        setRotateUpDown(true);
        setRotateStandHead(true);
        hasFired = false;
        hasReloaded = false;
        allowInvisibleStand = false;
        hasBaseStand = false;
        baseStandOffset = 0;
        baseStandModelNumber = 147;
        nextShotTime = System.currentTimeMillis();
        // PLACEHOLDER. SHOULD BE WE-WRITTEN
        EntityProjectile defaultProj = new EntityProjectile(new ItemStack(Material.GRASS_BLOCK));
        defaultProj.projectileCount = 2;
        defaultProj.entityType = EntityType.SNOWBALL;
        defaultProj.particleType = Particle.WHITE_ASH;
        defaultProj.soundType = Sound.ENTITY_BLAZE_SHOOT;

        // set passed parameters as object variables
        this.setEngineName(name);
        this.setFuelItem(fuelItem);
        this.setCustomModelID(customModelID);

        // set projectiles
        if (this.getProjectiles() == null || this.getProjectiles().isEmpty()) {
            this.getProjectiles().put(new ItemStack(Material.GUNPOWDER), defaultProj);
        } else {
            this.setProjectiles(projectiles);
        }
    }

    public boolean equals(String EquipmentId) {
        return id.equals(EquipmentId);
    }

    public boolean isLoaded() {
        return hasPropellant() && hasAmmunition();
    }

    public boolean hasPropellant() {
        return getAmmoHolder().getLoadedFuel() > 0;
    }

    public boolean hasAmmunition() {
        return getAmmoHolder().getLoadedProjectile() >= 1;
    }

    public boolean canLoadFuel() {
        return getAmmoHolder().getLoadedFuel() < getMaxFuel();
    }

    public boolean loadFuel(Entity player) {
        if (getAmmoHolder().getLoadedFuel() == getMaxFuel()) {
            return true;
        }
        if (!(player instanceof Player)) {
            getAmmoHolder().setLoadedFuel(getMaxFuel());
            return true;
        }
        if (((Player) player).getInventory().containsAtLeast(getFuelItem(), 1) || getFuelItem().getType() == Material.AIR) {
            if (canLoadFuel()) {
                this.getAmmoHolder().setLoadedFuel(this.getAmmoHolder().getLoadedFuel() + 1);
                //SaveState();
                ((Player) player).getInventory().removeItem(getFuelItem());
                player.sendMessage("§eLoaded " + getAmmoHolder().getLoadedFuel() + "/" + getMaxFuel());
                return true;
            } else {
                return false;
            }
        }

        return false;

    }

    public boolean LoadProjectile(Entity player, ItemStack itemInHand) {
        if (itemInHand.getAmount() <= 0) {
            return false;
        }
        if (!(player instanceof Player)) {
            getAmmoHolder().setLoadedProjectile(1);
            getAmmoHolder().setLoadedFuel(getMaxFuel());
            getAmmoHolder().setMaterialName(itemInHand);
            return true;
        }
        for (ItemStack im : getProjectiles().keySet()) {
            if (im.getType().equals(itemInHand.getType()) && getAmmoHolder().getLoadedProjectile() == 0) {
                getAmmoHolder().setLoadedProjectile(1);
                getAmmoHolder().setMaterialName(itemInHand);
                player.sendMessage("§eAdding Ammunition to Weapon");
                itemInHand.setAmount(itemInHand.getAmount() - 1);
                return true;
            }
        }
        return false;
    }

    public Location GetFireLocation(LivingEntity living) {
        if (living == null)
            return null;
        Location loc = living.getEyeLocation();
        Vector direction = living.getLocation().getDirection().multiply(getXOffset());
        loc.add(direction);
        if (getYOffset() > 0) {
            loc.setY(loc.getY() + getYOffset());
        } else {
            loc.setY(loc.getY() - getYOffset());
        }
        loc.setPitch(loc.getPitch());
        return loc;
    }

    public void ShowFireLocation(Entity player) {
        LivingEntity living = (LivingEntity) getEntity();
        if (player instanceof Player) {
            if (living != null) {
                Location origin = this.GetFireLocation(living);
                Vector direction = origin.getDirection();
                direction.multiply(5 /* the range */);
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
            amount = this.getShotAmount();
        this.setShotAmount(amount);
        if (System.currentTimeMillis() < nextShotTime) {
            return;
        }
        if (getAmmoHolder().getLoadedFuel() <= 0)
            getAmmoHolder().setLoadedFuel(0);
        float loadedFuel = getAmmoHolder().getLoadedFuel();
        ItemStack LoadedProjectile = getAmmoHolder().getMaterialName();

        LivingEntity living = (LivingEntity) getEntity();
        if (living == null || living.getEquipment() == null || living.getEquipment().getHelmet() == null || living.getEquipment().getHelmet().getItemMeta() == null) {
            return;
        }

        if (living.getEquipment().getHelmet().getItemMeta().getCustomModelData() != getReadyModelNumber() || getAmmoHolder().getLoadedProjectile() == 0) {
            if (!isSetModelNumberWhenFullyLoaded()) {
                if (player instanceof Player) {
                    player.sendMessage("§eCannot fire yet!");
                }
                return;
            } else {
                if (isSetModelNumberWhenFullyLoaded() && living.getEquipment().getHelmet().getItemMeta().getCustomModelData() != getPreFireModelNumber()) {
                    if (player instanceof Player) {
                        player.sendMessage("§eCannot fire yet!");
                    }
                    return;
                }
            }
        }
        getAmmoHolder().setLoadedFuel(0);
        getAmmoHolder().setLoadedProjectile(0);
        getAmmoHolder().setMaterialName(new ItemStack(Material.AIR));


        setWorldName(getEntity().getWorld().getName());
        nextShotTime = System.currentTimeMillis() + 1000;
        //for (int i = 0; i <= this.shotAmount /* range */; i += 1) {
        if (living == null || living.isDead()) {
            return;
        }
        if (isCycleThroughModelsWhileFiring()) {

            this.taskNumber = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(SiegeEngines.getInstance(), () -> {
                if (living == null || living.isDead()) {
                    Bukkit.getServer().getScheduler().cancelTask(taskNumber);
                    return;
                }

                if (hasFired) {
                    Bukkit.getServer().getScheduler().cancelTask(taskNumber);
                    taskNumber = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(SiegeEngines.getInstance(), () -> {
                        if (living == null || living.isDead()) {
                            Bukkit.getServer().getScheduler().cancelTask(taskNumber);
                            return;
                        }
                        //	player.sendMessage("§etask");
                        if (hasReloaded) {
                            Bukkit.getServer().getScheduler().cancelTask(taskNumber);
                            hasReloaded = false;
                            hasFired = false;
                            nextModelNumber = 0;
                            SiegeEnginesUtil.UpdateEntityIdModel(getEntity(), getReadyModelNumber(), getWorldName());
                            taskNumber = 0;
                        } else {
                            //firing stages
                            if (isSetModelNumberWhenFullyLoaded()) {
                                hasReloaded = true;
                            } else {
                                if (nextModelNumber - 1 <= getFiringModelNumbers().size() && nextModelNumber - 1 >= 0) {
                                    int modelData = getFiringModelNumbers().get(nextModelNumber - 1);
                                    SiegeEnginesUtil.UpdateEntityIdModel(getEntity(), modelData, getWorldName());
                                    nextModelNumber -= 1;

                                } else {
                                    hasReloaded = true;
                                }
                            }
                        }
                    }, 0, getMillisecondsBetweenReloadingStages());

                } else {
                    //firing stages
                    if (nextModelNumber < getFiringModelNumbers().size()) {

                        int modelData = getFiringModelNumbers().get(nextModelNumber);
                        //	player.sendMessage("§e" + modelData);
                        SiegeEnginesUtil.UpdateEntityIdModel(getEntity(), modelData, getWorldName());
                        if (modelData == getModelNumberToFireAt()) {
                            //	player.sendMessage("§efiring" + modelData);
                            SiegeEngineProjectile projType = null;
                            if (LoadedProjectile == null) return;
                            if (LoadedProjectile.getType() == Material.AIR) return;
                            if (LoadedProjectile.getType() == Material.FIREWORK_ROCKET) {
                                projType = FireworkProjectile.getDefaultRocketShot(LoadedProjectile);
                            } else {
                                projType = getProjectiles().get(LoadedProjectile);
                            }
                            if (projType == null) return;
                            projType.Shoot(player, getEntity(), this.GetFireLocation(living), loadedFuel * getVelocityPerFuel());
                        }
                        nextModelNumber += 1;
                    } else {
                        hasFired = true;

                    }
                }
            }, 0, getMillisecondsBetweenFiringStages());
        } else {
            taskNumber = Bukkit.getServer().getScheduler().scheduleSyncDelayedTask(SiegeEngines.getInstance(), () -> {
                //player.sendMessage("§etask");
                if (living == null || living.isDead()) {
                    Bukkit.getServer().getScheduler().cancelTask(taskNumber);
                    return;
                }

                Location loc = living.getEyeLocation();
                Vector direction = getEntity().getLocation().getDirection().multiply(getXOffset());

                loc.add(direction);

                nextModelNumber = 0;
                SiegeEngineProjectile projType = null;
                if (LoadedProjectile == null) return;
                if (LoadedProjectile.getType() == Material.AIR) return;
                if (LoadedProjectile.getType() == Material.FIREWORK_ROCKET) {
                    projType = FireworkProjectile.getDefaultRocketShot(LoadedProjectile);
                } else {
                    projType = getProjectiles().get(LoadedProjectile);
                }
                if (projType == null) return;
                projType.Shoot(player, getEntity(), this.GetFireLocation(living), loadedFuel * getVelocityPerFuel());
            }, (long) delay);

        }
    }

    @SuppressWarnings("deprecation")
    public boolean place(Entity player, Location l) {
        return place(player, l, null);
    }

    @SuppressWarnings("deprecation")
    public boolean place(Entity player, Location l, Entity mount) {
        l.add(0.5, 0, 0.5);
        NamespacedKey key = new NamespacedKey(SiegeEngines.getInstance(), "siege_engines");
        if (this == null || !this.getEnabled()) {
            return false;
        }
        l.setY(l.getY() + 1);
        l.setDirection(player.getFacing().getDirection());
        ItemStack item = new ItemStack(Material.CARVED_PUMPKIN);
        ItemMeta meta = item.getItemMeta();
        String id = "";
        Entity entity3 = null;
        for (Entity enti : l.getWorld().getNearbyEntities(l, Config.placementDensity, Config.placementDensity, Config.placementDensity)) {
            if (SiegeEngines.activeSiegeEngines.containsKey(enti.getUniqueId())) {
                return false;
            }
        }
        this.setAmmoHolder(new SiegeEngineAmmoHolder());
        if (this.hasBaseStand) {

            Location l2 = l;
            l2.setY(l.getY() + this.baseStandOffset);
            entity3 = player.getWorld().spawnEntity(l, EntityType.ARMOR_STAND);

            LivingEntity ent = (LivingEntity) entity3;
            ArmorStand stand = (ArmorStand) ent;
            id = entity3.getUniqueId().toString();
            meta.setCustomModelData(this.baseStandModelNumber);
            stand.addEquipmentLock(EquipmentSlot.HEAD, LockType.REMOVING_OR_CHANGING);
            stand.addEquipmentLock(EquipmentSlot.LEGS, LockType.ADDING_OR_CHANGING);
            stand.addEquipmentLock(EquipmentSlot.CHEST, LockType.ADDING_OR_CHANGING);
            stand.addEquipmentLock(EquipmentSlot.FEET, LockType.ADDING_OR_CHANGING);
            stand.addEquipmentLock(EquipmentSlot.HAND, LockType.REMOVING_OR_CHANGING);
            stand.addEquipmentLock(EquipmentSlot.OFF_HAND, LockType.ADDING_OR_CHANGING);
            stand.addEquipmentLock(EquipmentSlot.HAND, LockType.REMOVING_OR_CHANGING);
            stand.addEquipmentLock(EquipmentSlot.OFF_HAND, LockType.REMOVING_OR_CHANGING);
            stand.setBasePlate(false);
            item.setItemMeta(meta);
            stand.setInvisible(this.allowInvisibleStand);
            ent.getEquipment().setHelmet(item);
            stand.setGravity(false);
            stand.setMarker(true);
        }
        l.setY(l.getY() + this.getPlacementOffsetY());
        Entity entity2 = player.getWorld().spawnEntity(l, EntityType.ARMOR_STAND);
        if (id != "") {
            entity2.getPersistentDataContainer().set(key, PersistentDataType.STRING, id);
        }
        meta.setCustomModelData(this.getReadyModelNumber());
        meta.setDisplayName(this.getItemName());
        meta.setLore(this.getItemLore());
        item.setItemMeta(meta);


        LivingEntity ent = (LivingEntity) entity2;
        ArmorStand stand = (ArmorStand) ent;
        this.setEntity(entity2);

        this.setEntityId(entity2.getUniqueId());
        stand.addEquipmentLock(EquipmentSlot.HEAD, LockType.REMOVING_OR_CHANGING);
        stand.addEquipmentLock(EquipmentSlot.LEGS, LockType.ADDING_OR_CHANGING);
        stand.addEquipmentLock(EquipmentSlot.CHEST, LockType.ADDING_OR_CHANGING);
        stand.addEquipmentLock(EquipmentSlot.FEET, LockType.ADDING_OR_CHANGING);
        stand.addEquipmentLock(EquipmentSlot.HAND, LockType.REMOVING_OR_CHANGING);
        stand.addEquipmentLock(EquipmentSlot.OFF_HAND, LockType.ADDING_OR_CHANGING);
        stand.addEquipmentLock(EquipmentSlot.HAND, LockType.REMOVING_OR_CHANGING);
        stand.addEquipmentLock(EquipmentSlot.OFF_HAND, LockType.REMOVING_OR_CHANGING);
        stand.setInvisible(this.allowInvisibleStand);
        stand.setBasePlate(true);

        stand.setGravity(false);
        ent.getEquipment().setHelmet(item);
        /*if (SiegeEngines.siegeEngineEntitiesPerPlayer.containsKey(player.getUniqueId())) {
            List<Entity> entities = SiegeEngines.siegeEngineEntitiesPerPlayer.get(player.getUniqueId());
            entities.add(entity2);
            SiegeEngines.siegeEngineEntitiesPerPlayer.put(player.getUniqueId(), entities);
        } else {
            List<Entity> newList = new ArrayList<Entity>();
            newList.add(entity2);
            SiegeEngines.siegeEngineEntitiesPerPlayer.put(player.getUniqueId(), newList);
        }*/
        stand.setMaxHealth(baseHealth);
        stand.setHealth(baseHealth);
        SiegeEngines.activeSiegeEngines.put(stand.getUniqueId(), this);
        if (mount != null && !(mount.isDead())) {
            entity2.setGravity(true);
            mount.addPassenger(entity2);
        }
        return true;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public void setEntityId(UUID entityId) {
        this.entityId = entityId;
    }

    public SiegeEngineType getType() {
        return type;
    }

    public void setType(SiegeEngineType type) {
        this.type = type;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public String getItemName() {
        return itemName;
    }

    public void setItemName(String itemName) {
        this.itemName = itemName;
    }

    public List<String> getItemLore() {
        return itemLore;
    }

    public void setItemLore(List<String> itemLore) {
        this.itemLore = itemLore;
    }

    public String getWorldName() {
        return worldName;
    }

    public void setWorldName(String worldName) {
        this.worldName = worldName;
    }

    public double getHealth() {
        return baseHealth;
    }

    public void setHealth(double health) {
        this.baseHealth = health;
    }

    public Entity getEntity() {
        return entity;
    }

    public void setEntity(Entity entity) {
        this.entity = entity;
    }

    public int getYOffset() {
        return yOffset;
    }

    public void setYOffset(int yOffset) {
        this.yOffset = yOffset;
    }

    public int getXOffset() {
        return xOffset;
    }

    public void setXOffset(int xOffset) {
        this.xOffset = xOffset;
    }

    public int getMaxFuel() {
        return maxFuel;
    }

    public void setMaxFuel(int maxFuel) {
        this.maxFuel = maxFuel;
    }

    public double getPlacementOffsetY() {
        return placementOffsetY;
    }

    public void setPlacementOffsetY(double placementOffsetY) {
        this.placementOffsetY = placementOffsetY;
    }

    public float getVelocityPerFuel() {
        return velocityPerFuel;
    }

    public void setVelocityPerFuel(float velocityPerFuel) {
        this.velocityPerFuel = velocityPerFuel;
    }

    public int getShotAmount() {
        return shotAmount;
    }

    public void setShotAmount(int shotAmount) {
        this.shotAmount = shotAmount;
    }

    public int getMillisecondsBetweenFiringStages() {
        return millisecondsBetweenFiringStages;
    }

    public void setMillisecondsBetweenFiringStages(int millisecondsBetweenFiringStages) {
        this.millisecondsBetweenFiringStages = millisecondsBetweenFiringStages;
    }

    public int getMillisecondsBetweenReloadingStages() {
        return millisecondsBetweenReloadingStages;
    }

    public void setMillisecondsBetweenReloadingStages(int millisecondsBetweenReloadingStages) {
        this.millisecondsBetweenReloadingStages = millisecondsBetweenReloadingStages;
    }

    public int getModelNumberToFireAt() {
        return modelNumberToFireAt;
    }

    public void setModelNumberToFireAt(int modelNumberToFireAt) {
        this.modelNumberToFireAt = modelNumberToFireAt;
    }

    public boolean isCycleThroughModelsWhileFiring() {
        return cycleThroughModelsWhileFiring;
    }

    public void setCycleThroughModelsWhileFiring(boolean cycleThroughModelsWhileFiring) {
        this.cycleThroughModelsWhileFiring = cycleThroughModelsWhileFiring;
    }

    public boolean isSetModelNumberWhenFullyLoaded() {
        return setModelNumberWhenFullyLoaded;
    }

    public void setSetModelNumberWhenFullyLoaded(boolean setModelNumberWhenFullyLoaded) {
        this.setModelNumberWhenFullyLoaded = setModelNumberWhenFullyLoaded;
    }

    public boolean isRotateSideways() {
        return rotateSideways;
    }

    public void setRotateSideways(boolean rotateSideways) {
        this.rotateSideways = rotateSideways;
    }

    public boolean isRotateUpDown() {
        return rotateUpDown;
    }

    public void setRotateUpDown(boolean rotateUpDown) {
        this.rotateUpDown = rotateUpDown;
    }

    public boolean isRotateStandHead() {
        return rotateStandHead;
    }

    public void setRotateStandHead(boolean rotateStandHead) {
        this.rotateStandHead = rotateStandHead;
    }

    public List<Integer> getFiringModelNumbers() {
        return firingModelNumbers;
    }

    public void setFiringModelNumbers(List<Integer> firingModelNumbers) {
        this.firingModelNumbers = firingModelNumbers;
    }

    public int getReadyModelNumber() {
        return readyModelNumber;
    }

    public void setReadyModelNumber(int readyModelNumber) {
        this.readyModelNumber = readyModelNumber;
    }

    public int getPreFireModelNumber() {
        return preFireModelNumber;
    }

    public void setPreFireModelNumber(int preFireModelNumber) {
        this.preFireModelNumber = preFireModelNumber;
    }

    public int getPreLoadModelNumber() {
        return preLoadModelNumber;
    }

    public void setPreLoadModelNumber(int preLoadModelNumber) {
        this.preLoadModelNumber = preLoadModelNumber;
    }

    public SiegeEngineAmmoHolder getAmmoHolder() {
        return ammoHolder;
    }

    public void setAmmoHolder(SiegeEngineAmmoHolder ammoHolder) {
        this.ammoHolder = ammoHolder;
    }

    public boolean isMountable() {
        return mountable;
    }

    public void setMountable(boolean canMount) {
        this.mountable = canMount;
    }

    public String getEngineName() {
        return engineName;
    }

    public void setEngineName(String engineName) {
        this.engineName = engineName;
    }

    public ItemStack getFuelItem() {
        return fuelItem;
    }

    public void setFuelItem(ItemStack fuelItem) {
        this.fuelItem = fuelItem;
    }

    public HashMap<ItemStack, SiegeEngineProjectile> getProjectiles() {
        return projectiles;
    }

    public void setProjectiles(HashMap<ItemStack, SiegeEngineProjectile> projectiles) {
        this.projectiles = projectiles;
    }

    public int getCustomModelID() {
        return customModelID;
    }

    public void setCustomModelID(int customModelID) {
        this.customModelID = customModelID;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
