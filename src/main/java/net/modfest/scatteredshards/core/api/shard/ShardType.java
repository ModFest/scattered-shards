package net.modfest.scatteredshards.core.api.shard;

import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.ScatteredShards;

public enum ShardType {

	VISITOR,
	CHALLENGE,
	SECRET;

	/*public static ShardType fromJson(JsonElement element) {
		String str = JsonHelper.asString(element, "shard type");
		return EnumUtils.getEnumIgnoreCase(ShardType.class, str);
	}*/

	private final String id = name().toLowerCase();
	private final Identifier backing = getTexture("backing");
	private final Identifier front = getTexture("front");

	public Identifier getShardId(Identifier setId) {
		return new Identifier(setId.getNamespace(), setId.getPath() + "_" + id);
	}

	private Identifier getTexture(String type) {
		return ScatteredShards.id("textures/gui/shards/" + id + "_" + type + ".png");
	}

	public String id() {
		return id;
	}

	public Identifier backing() {
		return backing;
	}

	public Identifier front() {
		return front;
	}
}
