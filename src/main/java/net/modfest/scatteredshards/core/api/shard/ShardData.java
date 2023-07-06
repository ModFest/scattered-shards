package net.modfest.scatteredshards.core.api.shard;

import com.google.gson.JsonObject;
import net.minecraft.util.JsonHelper;

public record ShardData(ShardType type, ShardInfo info, ShardIcon icon) {

	public static ShardData fromJson(JsonObject obj) {
		ShardType type = ShardType.fromJson(obj.get("type"));
		ShardInfo info = ShardInfo.fromJson(JsonHelper.getObject(obj, "info"));
		ShardIcon icon = ShardIcon.fromJson(JsonHelper.getObject(obj, "icon"));
		return new ShardData(type, info, icon);
	}
}
