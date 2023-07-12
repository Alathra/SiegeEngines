package siegeCore;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.reflect.TypeToken;
import com.google.gson.stream.JsonReader;

import CrunchProjectiles.CrunchProjectile;
import CrunchProjectiles.EntityProjectile;
import CrunchProjectiles.ExplosiveProjectile;
import CrunchProjectiles.PotionProjectile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.UUID;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;


public class StorageManager {

	private static File folder;
	private static File storage;
	private static Gson gson = new GsonBuilder().setPrettyPrinting().create();

	private static Plugin plugin;
	public static void setup(String path, Plugin pluginInput) {
		plugin = pluginInput;
		folder = new File(path);
		if (!folder.exists()) folder.mkdir();
	}

	//put this stuff in a try catch so i dont need a try catch everytime i call the methods
	public static SiegeEquipment load(String s) {
		storage = new File(s);
		FileReader reader;
		Deserializer deserializer = new Deserializer();
		gson = new GsonBuilder().setPrettyPrinting().registerTypeAdapter(CrunchProjectile.class, deserializer).create();
		SiegeEquipment data = null;
		if (!storage.exists()) {
			try {
				storage.createNewFile();
				FileWriter writer = new FileWriter(storage, false);
				writer.write(gson.toJson(new SiegeEquipment()));
				writer.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		try {
			reader = new FileReader(storage);
			JsonReader test = new JsonReader(reader);
			test.setLenient(true);
			Type temp = new TypeToken<SiegeEquipment>() {}.getType();
			data = gson.fromJson(test, temp);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return data;
	}
	
	public static void Save(SiegeEquipment equipment) {

		storage = new File(folder, "Example.json");
		FileWriter writer;
		try {
			writer = new FileWriter(storage, false);		
			writer.write(gson.toJson(equipment));
			writer.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	
	}
	public static class Deserializer implements JsonDeserializer<CrunchProjectile> {
	    private String TypeName = "ProjectileType";
	    private Gson gson = new Gson();

	    @Override
	    public CrunchProjectile deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) {
	        JsonObject Object = json.getAsJsonObject();
	        JsonElement type = Object.get(TypeName);
	        System.out.println(type.getAsString());
	        switch (type.getAsString()) {
	        case "Entity":
	            return gson.fromJson(Object, EntityProjectile.class);
	        case "Explosive":
	            return gson.fromJson(Object, ExplosiveProjectile.class);
	        case "Potion":
	            return gson.fromJson(Object, PotionProjectile.class);
	        }
			return null;
	  
	    }

	}
}