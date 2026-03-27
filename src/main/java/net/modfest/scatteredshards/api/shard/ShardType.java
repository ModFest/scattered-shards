package net.modfest.scatteredshards.api.shard;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Registry;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.impl.ColorCodec;

import java.util.Optional;

public record ShardType(int textColor, int glowColor, Optional<ShardDisplaySettings> displaySettings, Optional<ParticleType<?>> collectParticle, Optional<SoundEvent> collectSound, int listOrder) {

	public static final Codec<ShardType> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ColorCodec.CODEC.fieldOf("text_color").forGetter(ShardType::textColor),
		ColorCodec.CODEC.fieldOf("glow_color").forGetter(ShardType::glowColor),
		Codec.optionalField("display", ShardDisplaySettings.CODEC, false).forGetter(ShardType::displaySettings),
		Codec.optionalField("collect_particle", BuiltInRegistries.PARTICLE_TYPE.byNameCodec(), false).forGetter(ShardType::collectParticle),
		Codec.optionalField("collect_sound", SoundEvent.DIRECT_CODEC, false).forGetter(ShardType::collectSound),
		Codec.INT.fieldOf("list_order").forGetter(ShardType::listOrder)
	).apply(instance, ShardType::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ShardType> PACKET_CODEC = StreamCodec.composite(
		ByteBufCodecs.INT, ShardType::textColor,
		ByteBufCodecs.INT, ShardType::glowColor,
		ByteBufCodecs.optional(ShardDisplaySettings.PACKET_CODEC), ShardType::displaySettings,
		ByteBufCodecs.optional(ByteBufCodecs.fromCodecWithRegistries(BuiltInRegistries.PARTICLE_TYPE.byNameCodec())), ShardType::collectParticle,
		ByteBufCodecs.optional(SoundEvent.DIRECT_STREAM_CODEC), ShardType::collectSound,
		ByteBufCodecs.INT, ShardType::listOrder,
		ShardType::new
	);

	public static final SoundEvent COLLECT_VISITOR_SOUND = SoundEvent.createVariableRangeEvent(ScatteredShards.id("collect_visitor"));
	public static final SoundEvent COLLECT_CHALLENGE_SOUND = SoundEvent.createVariableRangeEvent(ScatteredShards.id("collect_challenge"));
	public static final SoundEvent COLLECT_SECRET_SOUND = SoundEvent.createVariableRangeEvent(ScatteredShards.id("collect_secret"));

	public static final ShardType MISSING = new ShardType(0xFFFFFF, 0xFF00FF, Optional.empty(), Optional.empty(), Optional.empty(), -1);
	public static final Identifier MISSING_ID = ScatteredShards.id("missing");

	public static Identifier createModId(Identifier shardTypeId, String modId) {
		return Identifier.fromNamespaceAndPath(modId, shardTypeId.toDebugFileName());
	}

	private static Identifier getTexture(Identifier id, String name) {
		return id.withPath("textures/gui/shards/" + id.getPath() + "_" + name + ".png");
	}

	public static Identifier getBackingTexture(Identifier id) {
		return getTexture(id, "backing");
	}

	public static Identifier getFrontTexture(Identifier id) {
		return getTexture(id, "front");
	}

	public static Identifier getMiniFrontTexture(Identifier id) {
		return getTexture(id, "mini_front");
	}

	public static Identifier getMiniBackingTexture(Identifier id) {
		return getTexture(id, "mini_backing");
	}

	public static Component getDescription(Identifier id) {
		return Component.translatable(id.toLanguageKey("shard_type", "description"));
	}

	public ShardDisplaySettings getDisplaySettings() {
		return this.displaySettings.orElse(ShardDisplaySettings.DEFAULT);
	}
	public ShardIconOffsets getOffsets() {
		return this.getDisplaySettings().getOffsets();
	}
	public ShardTextureSettings getTextureSettings() {
		return this.getDisplaySettings().getTextureSettings();
	}

	public CompoundTag toNbt() {
		return (CompoundTag) CODEC.encodeStart(NbtOps.INSTANCE, this).result().orElseThrow();
	}

	public JsonObject toJson() {
		return (JsonObject) CODEC.encodeStart(JsonOps.INSTANCE, this).result().orElseThrow();
	}

	public static ShardType fromNbt(CompoundTag tag) {
		return CODEC.parse(NbtOps.INSTANCE, tag).result().orElseThrow();
	}

	public static ShardType fromJson(JsonObject obj) {
		return CODEC.parse(JsonOps.INSTANCE, obj).result().orElseThrow();
	}

	public static void register() {
		Registry.register(BuiltInRegistries.SOUND_EVENT, COLLECT_VISITOR_SOUND.location(), COLLECT_VISITOR_SOUND);
		Registry.register(BuiltInRegistries.SOUND_EVENT, COLLECT_CHALLENGE_SOUND.location(), COLLECT_CHALLENGE_SOUND);
		Registry.register(BuiltInRegistries.SOUND_EVENT, COLLECT_SECRET_SOUND.location(), COLLECT_SECRET_SOUND);
	}
}
