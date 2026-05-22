package net.modfest.scatteredshards;

import folk.sisby.kaleido.api.ReflectiveConfig;
import folk.sisby.kaleido.lib.quiltconfig.api.annotations.Comment;
import folk.sisby.kaleido.lib.quiltconfig.api.values.TrackedValue;

public class ScatteredShardsConfig extends ReflectiveConfig {
	@Comment("Whether to replace advancements button with a shard collection button")
	public final TrackedValue<Boolean> replace_advancements = value(true);

	@Comment("Whether to use unique outline textures for each mini shard and for front and back variations")
	public final TrackedValue<Boolean> use_unique_mini_outlines = value(false);
}
