package net.modfest.scatteredshards.api.impl;

import com.google.gson.JsonObject;

import net.minecraft.server.MinecraftServer;
import net.minecraft.util.WorldSavePath;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;

public class JsonExport {
	public static final String SHARDS_KEY = "shards";
	public static final String SHARD_SETS_KEY = "shard-sets";
	
	public void save(MinecraftServer server) {
		System.out.println("Wanting to save "+ScatteredShardsAPI.getServerLibrary().shards().size()+" shards...");
		System.out.println("Level Data is at: "+server.getSavePath(WorldSavePath.LEVEL_DAT));
		
		JsonObject root = new JsonObject();
		JsonObject shards = new JsonObject();
		root.add(SHARDS_KEY, shards);
		
		ScatteredShardsAPI.getServerLibrary().shards().forEach((id, shard) -> {
			shards.add(id.toString(), shard.toJson());
		});
	}
	
	/*
	public void load(MinecraftServer server) {
		System.out.println("Level Data is at: "+server.getSavePath(WorldSavePath.LEVEL_DAT));
		
		
	}*/
	
}
