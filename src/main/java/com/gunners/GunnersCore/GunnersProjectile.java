package com.gunners.GunnersCore;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public interface GunnersProjectile {
	public void Shoot(Entity player, Entity entity, Location fireLocation, Float velocity);
	public String ProjectileType = "Default";
}

