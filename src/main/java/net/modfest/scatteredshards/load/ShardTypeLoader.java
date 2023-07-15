package net.modfest.scatteredshards.load;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.resource.ResourceType;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.profiler.Profiler;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.impl.ScatteredShardsAPIImpl;
import net.modfest.scatteredshards.core.api.shard.ShardType;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.resource.loader.api.ResourceLoader;
import org.quiltmc.qsl.resource.loader.api.reloader.IdentifiableResourceReloader;

import java.util.HashMap;
import java.util.Map;

public class ShardTypeLoader extends JsonDataLoader implements IdentifiableResourceReloader {

	public static final String TYPE = "shard_type";
	public static final Identifier ID = ScatteredShards.id(TYPE);

	public static final Map<Identifier, ShardType> MAP = new HashMap<>();

	public ShardTypeLoader() {
		super(new Gson(), TYPE);
	}

	@Override
	public @NotNull Identifier getQuiltId() {
		return ID;
	}

	@Override
	protected void apply(Map<Identifier, JsonElement> cache, ResourceManager manager, Profiler profiler) {
		MAP.clear();
		int successes = 0;
		for (var entry : cache.entrySet()) {
			try {
				JsonObject obj = JsonHelper.asObject(entry.getValue(), "shard type object");
				MAP.put(entry.getKey(), ShardType.fromJson(obj));
				successes++;
			} catch (Exception ex) {
				ScatteredShards.LOGGER.error("Failed to load shard type '" + entry.getKey() + "':", ex);
			}
		}
		ScatteredShards.LOGGER.info("Loaded " + successes + " shard type" + (successes == 1 ? "" : "s"));
		ScatteredShardsAPIImpl.updateShardTypes();
	}

	public static void register() {
		ResourceLoader.get(ResourceType.SERVER_DATA).registerReloader(new ShardTypeLoader());
	}
}
