package CrunchProjectiles;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public interface CrunchProjectile {
 
	public void Shoot(Player player, Entity entity, Location fireLocation, Float velocity);
	public String ProjectileType = "Default";
}

