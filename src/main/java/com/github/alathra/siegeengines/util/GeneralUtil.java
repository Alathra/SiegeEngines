package com.github.alathra.siegeengines.util;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class GeneralUtil {

    public static ArrayList<Block> getSphere(final Location center, final int radius) {
        ArrayList<Block> sphere = new ArrayList<Block>();
        for (int Y = -radius; Y < radius; Y++) {
            for (int X = -radius; X < radius; X++) {
                for (int Z = -radius; Z < radius; Z++) {
                    if (Math.sqrt((X * X) + (Y * Y) + (Z * Z)) <= radius) {
                        final Block block = center.getWorld().getBlockAt(X + center.getBlockX(), Y + center.getBlockY(),
                            Z + center.getBlockZ());
                        if (block.getType() == Material.AIR) {
                            sphere.add(block);
                        }
                    }
                }
            }
        }
        return sphere;
    }

    public static boolean hasItem(Inventory inv, ItemStack m) {
        return inv.containsAtLeast(m, m.getAmount());
    }

    public static Object getRandomElement(List<?> list) {
        Random rand = new Random();
        return list.get(rand.nextInt(list.size()));
    }

    public static ItemStack[] updateContents(Inventory inv, ItemStack m, int toRemove) {
        ItemStack[] contents = inv.getStorageContents();
        for (int i = 0; i < contents.length; i++) {
            if (contents[i] == null)
                continue;
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
            if (toRemove <= 0)
                break;
        }
        return contents;
    }

    public static Vector getDirectionBetweenLocations(Location Start, Location End) {
        Vector from = Start.toVector();
        Vector to = End.toVector();
        return to.subtract(from);
    }
}
