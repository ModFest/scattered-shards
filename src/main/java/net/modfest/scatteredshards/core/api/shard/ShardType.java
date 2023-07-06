package net.modfest.scatteredshards.core.api.shard;

import com.google.gson.JsonElement;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.modfest.scatteredshards.ScatteredShards;
import org.apache.commons.lang3.EnumUtils;

public enum ShardType {

	VISITOR,
	CHALLENGE,
	SECRET;

	public static ShardType fromJson(JsonElement element) {
		String str = JsonHelper.asString(element, "shard type");
		return EnumUtils.getEnumIgnoreCase(ShardType.class, str);
	}

	private Identifier getTexture(String type) {
		return ScatteredShards.id("textures/gui/shards/" + name().toLowerCase() + "_" + type + ".png");
	}

	public Identifier getBacking() {
		return getTexture("backing");
	}

	public Identifier getFront() {
		return getTexture("front");
	}
}
