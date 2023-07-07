package net.modfest.scatteredshards.load;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.impl.ScatteredShardsAPIImpl;
import net.modfest.scatteredshards.core.api.shard.Shard;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.resource.loader.api.ResourceLoader;
import org.quiltmc.qsl.resource.loader.api.reloader.IdentifiableResourceReloader;

import java.util.HashMap;
import java.util.Map;

public class ShardSetLoader extends JsonDataLoader implements IdentifiableResourceReloader {

	public static final String TYPE = "shard_sets";
	public static final Identifier ID = ScatteredShards.id(TYPE);
	
	public static final Map<Identifier, Shard> BY_ID = new HashMap<>();
	public static final Multimap<Identifier, Shard> BY_SHARD_SET = MultimapBuilder.hashKeys().arrayListValues().build();

	public ShardSetLoader() {
		super(new Gson(), TYPE);
	}

	@Override
	public @NotNull Identifier getQuiltId() {
		return ID;
	}

	@Override
	protected void apply(Map<Identifier, JsonElement> cache, ResourceManager manager, Profiler profiler) {
		ScatteredShardsAPIImpl.onReload();
		BY_ID.clear();
		BY_SHARD_SET.clear();
		int successes = 0;
		for (var entry : cache.entrySet()) {
			if (entry.getValue() instanceof JsonObject shardListObj) {
				for(var shardEntry : shardListObj.entrySet()) {
					if (shardEntry.getValue() instanceof JsonObject shardObj) {
						try {
							Shard shard = Shard.fromJson(shardObj);
							BY_ID.put(new Identifier(shardEntry.getKey()), shard);
							BY_SHARD_SET.put(shard.shardType(), shard);
							successes++;
						} catch (Exception ex) {
							ScatteredShards.LOGGER.error("Failed to load shard set '" + entry.getKey() + "':", ex);
						}
					}
				}
			}
		}
		ScatteredShards.LOGGER.info("Loaded " + successes + " shard" + (successes == 1 ? "" : "s"));
	}

	public static void register() {
		ResourceLoader.get(ResourceType.SERVER_DATA).registerReloader(new ShardSetLoader());
	}
}
