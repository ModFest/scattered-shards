package net.modfest.scatteredshards.core.api.shard;

import com.google.gson.JsonObject;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;

public record ShardType(int textColor) {

	public static final ShardType VISITOR = new ShardType(0x6DE851);
	public static final ShardType CHALLENGE = new ShardType(0x5174E8);
	public static final ShardType SECRET = new ShardType(0xEB4034);
	public static final ShardType MISSING = new ShardType(0xFFFFFF);

	public Identifier getId() {
		return ScatteredShardsAPI.getShardTypes().inverse().get(this);
	}

	private Identifier getTexture(String name) {
		Identifier id = getId();
		return id.withPath("textures/gui/shards/" + id.getPath() + "_" + name + ".png");
	}

	public Identifier getBackingTexture() {
		return getTexture("backing");
	}

	public Identifier getFrontTexture() {
		return getTexture("front");
	}

	public Text getDescription() {
		return Text.translatable(getId().toTranslationKey("shard_type", "description"));
	}

	public void write(PacketByteBuf buf) {
		buf.writeInt(textColor);
	}

	public static ShardType read(PacketByteBuf buf) {
		return new ShardType(buf.readInt());
	}

	public static ShardType fromJson(JsonObject obj) {
		return new ShardType(JsonHelper.getInt(obj, "text_color"));
	}

	public static void register() {
		ScatteredShardsAPI.registerShardType(ScatteredShards.id("visitor"), VISITOR);
		ScatteredShardsAPI.registerShardType(ScatteredShards.id("challenge"), CHALLENGE);
		ScatteredShardsAPI.registerShardType(ScatteredShards.id("secret"), SECRET);
		ScatteredShardsAPI.registerShardType(ScatteredShards.id("missing"), MISSING);
	}
}
