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
import net.modfest.scatteredshards.core.api.shard.ShardSet;
import org.jetbrains.annotations.NotNull;
import org.quiltmc.qsl.resource.loader.api.ResourceLoader;
import org.quiltmc.qsl.resource.loader.api.reloader.IdentifiableResourceReloader;

import java.util.HashMap;
import java.util.Map;

public class ShardSetLoader extends JsonDataLoader implements IdentifiableResourceReloader {

	public static final String TYPE = "shard_sets";
	public static final Identifier ID = ScatteredShards.id(TYPE);

	public static final Map<Identifier, ShardSet> LOADED_SHARD_SETS = new HashMap<>();

	public ShardSetLoader() {
		super(new Gson(), TYPE);
	}

	@Override
	public @NotNull Identifier getQuiltId() {
		return ID;
	}

	@Override
	protected void apply(Map<Identifier, JsonElement> cache, ResourceManager manager, Profiler profiler) {
		LOADED_SHARD_SETS.clear();
		int successes = 0;
		for (var pair : cache.entrySet()) {
			try {
				JsonObject obj = JsonHelper.asObject(pair.getValue(), "shard set");
				LOADED_SHARD_SETS.put(pair.getKey(), ShardSet.fromJson(pair.getKey(), obj));
				successes++;
			}
			catch (Exception e) {
				ScatteredShards.LOGGER.error("Failed to load shard set '" + pair.getKey() + "':", e);
			}
		}
		ScatteredShards.LOGGER.info("Loaded " + successes + " shard set" + (successes == 1 ? "" : "s"));
	}

	public static void register() {
		ResourceLoader.get(ResourceType.SERVER_DATA).registerReloader(new ShardSetLoader());
	}
}
