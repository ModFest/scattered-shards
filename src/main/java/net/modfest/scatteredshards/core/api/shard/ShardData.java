package net.modfest.scatteredshards.core.api.shard;

import com.google.gson.JsonObject;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

public record ShardData(Identifier id, ShardType type, ShardInfo info, ShardIcon icon) {

	public static ShardData fromJson(Identifier id, ShardType type,JsonObject obj) {
		ShardInfo info = ShardInfo.fromJson(JsonHelper.getObject(obj, "info"));
		ShardIcon icon = ShardIcon.fromJson(JsonHelper.getObject(obj, "icon"));
		return new ShardData(id, type, info, icon);
	}
}
