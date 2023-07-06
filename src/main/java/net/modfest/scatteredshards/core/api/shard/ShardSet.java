package net.modfest.scatteredshards.core.api.shard;

import com.google.gson.JsonObject;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;

import java.util.*;

public record ShardSet(ShardData visitor, Optional<ShardData> challenge, Optional<ShardData> secret) {

	public ShardSet {
		Objects.requireNonNull(visitor);
		if (secret.isPresent() && challenge.isEmpty()) {
			throw new IllegalStateException("'challenge' must exist in order for 'secret' to exist");
		}
	}

	public List<ShardData> getShards() {
		List<ShardData> list = new ArrayList<>(List.of(visitor));
		challenge.ifPresent(list::add);
		secret.ifPresent(list::add);
		return Collections.unmodifiableList(list);
	}

	private static ShardData shardFromJson(Identifier id, ShardType type, JsonObject parentObj) {
		if (!parentObj.has(type.id())) {
			return null;
		}
		JsonObject shardObj = JsonHelper.getObject(parentObj, type.id());
		return ShardData.fromJson(type.getShardId(id), type, shardObj);
	}

	public static ShardSet fromJson(Identifier id, JsonObject obj) {
		ShardData visitor = shardFromJson(id, ShardType.VISITOR, obj);
		ShardData challenge = shardFromJson(id, ShardType.CHALLENGE, obj);
		ShardData secret = shardFromJson(id, ShardType.SECRET, obj);
		return new ShardSet(visitor, Optional.ofNullable(challenge), Optional.ofNullable(secret));
	}
}
