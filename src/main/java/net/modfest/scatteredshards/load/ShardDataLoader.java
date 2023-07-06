package net.modfest.scatteredshards.world.load;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import net.minecraft.resource.JsonDataLoader;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.profiler.Profiler;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.core.api.shard.ShardData;
import org.jetbrains.annotations.NotNull;
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
	protected void apply(Map<Identifier, JsonElement> cache, ResourceManager manager, Profiler profiler) {

	}

	@Override
	public @NotNull Identifier getQuiltId() {
		return ID;
	}
}
