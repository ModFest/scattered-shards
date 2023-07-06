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
import net.modfest.scatteredshards.core.api.shard.ShardData;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.resource.loader.api.ResourceLoader;
import org.quiltmc.qsl.resource.loader.api.reloader.IdentifiableResourceReloader;

import java.util.HashMap;
import java.util.Map;

public class ShardDataLoader extends JsonDataLoader implements IdentifiableResourceReloader {

	public static final String TYPE = "shards";
	public static final Identifier ID = ScatteredShards.id(TYPE);

	public static Map<Identifier, ShardData> data = new HashMap<>();

	public ShardDataLoader() {
		super(new Gson(), TYPE);
	}

	@Override
	public @NotNull Identifier getQuiltId() {
		return ID;
	}

	@Override
	protected void apply(Map<Identifier, JsonElement> cache, ResourceManager manager, Profiler profiler) {
		data.clear();
		int successes = 0;
		for (var pair : cache.entrySet()) {
			try {
				JsonObject obj = JsonHelper.asObject(pair.getValue(), "shard data");
				data.put(pair.getKey(), ShardData.fromJson(obj));
				successes++;
			}
			catch (Exception e) {
				ScatteredShards.LOGGER.error("Failed to load shard data '" + pair.getKey() + "':", e);
			}
		}
		ScatteredShards.LOGGER.info("Loaded " + successes + " shard data object" + (successes == 1 ? "" : "s"));
	}

	public static void register() {
		ResourceLoader.get(ResourceType.SERVER_DATA).registerReloader(new ShardDataLoader());
	}
}
