package net.modfest.scatteredshards.shard;

import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.ScatteredShards;

public enum ShardType {

	VISITOR,
	CHALLENGE,
	SECRET;

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
