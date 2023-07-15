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

	/*public static final RegistryKey<Registry<ShardType>> REGISTRY_KEY = RegistryKey.ofRegistry(ScatteredShards.id("shard_type"));

	public static final Codec<ShardType> CODEC = RecordCodecBuilder.create(instance -> {
		return instance.group(
				Codec.INT.fieldOf("text_color").forGetter(ShardType::textColor)
		).apply(instance, ShardType::new);
	});*/

	public Identifier getId() {
		return ScatteredShardsAPI.getShardTypes().inverse().get(this);
	}

	private Identifier getTexture(String name) {
		Identifier id = getId();
		return id.withPath("textures/gui/shards/" + id.getPath() + "_" + name + ".png");
	}

	/*public static ShardType fromJson(JsonElement element) {
		String str = JsonHelper.asString(element, "shard type");
		return EnumUtils.getEnumIgnoreCase(ShardType.class, str);
	}*/

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

	/*public static void register() {
		DynamicMetaRegistry.registerSynced(REGISTRY_KEY, CODEC);
		DynamicRegistryManager.fromRegistryOfRegistries(Registries.REGISTRY).get(REGISTRY_KEY).
	}*/

	public static void register() {
		ScatteredShardsAPI.registerShardType(ScatteredShards.id("visitor"), VISITOR);
		ScatteredShardsAPI.registerShardType(ScatteredShards.id("challenge"), CHALLENGE);
		ScatteredShardsAPI.registerShardType(ScatteredShards.id("secret"), SECRET);
	}
}
