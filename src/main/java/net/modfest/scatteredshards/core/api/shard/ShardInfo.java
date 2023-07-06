package net.modfest.scatteredshards.core.api.shard;

import com.google.gson.JsonObject;
import net.minecraft.text.Text;

public record ShardInfo(Text name, Text lore, Text hint) {

	public static ShardInfo fromJson(JsonObject obj) {
		Text name = Text.Serializer.fromJson(obj.get("name"));
		Text lore = Text.Serializer.fromJson(obj.get("lore"));
		Text hint = Text.Serializer.fromJson(obj.get("hint"));
		return new ShardInfo(name, lore, hint);
	}
}
