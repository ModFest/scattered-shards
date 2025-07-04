package net.modfest.scatteredshards.api.shard;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.impl.ColorCodec;

import java.util.Optional;

public record ShardType(int textColor, int glowColor, Optional<ShardDisplaySettings> displaySettings, Optional<ParticleType<?>> collectParticle, Optional<SoundEvent> collectSound, int listOrder) {

	public static final Codec<ShardType> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		ColorCodec.CODEC.fieldOf("text_color").forGetter(ShardType::textColor),
		ColorCodec.CODEC.fieldOf("glow_color").forGetter(ShardType::glowColor),
		Codec.optionalField("display", ShardDisplaySettings.CODEC, false).forGetter(ShardType::displaySettings),
		Codec.optionalField("collect_particle", Registries.PARTICLE_TYPE.getCodec(), false).forGetter(ShardType::collectParticle),
		Codec.optionalField("collect_sound", SoundEvent.CODEC, false).forGetter(ShardType::collectSound),
		Codec.INT.fieldOf("list_order").forGetter(ShardType::listOrder)
	).apply(instance, ShardType::new));

	public static final PacketCodec<RegistryByteBuf, ShardType> PACKET_CODEC = PacketCodec.tuple(
		PacketCodecs.INTEGER, ShardType::textColor,
		PacketCodecs.INTEGER, ShardType::glowColor,
		PacketCodecs.optional(ShardDisplaySettings.PACKET_CODEC), ShardType::displaySettings,
		PacketCodecs.optional(PacketCodecs.registryCodec(Registries.PARTICLE_TYPE.getCodec())), ShardType::collectParticle,
		PacketCodecs.optional(SoundEvent.PACKET_CODEC), ShardType::collectSound,
		PacketCodecs.INTEGER, ShardType::listOrder,
		ShardType::new
	);

	public static final SoundEvent COLLECT_VISITOR_SOUND = SoundEvent.of(ScatteredShards.id("collect_visitor"));
	public static final SoundEvent COLLECT_CHALLENGE_SOUND = SoundEvent.of(ScatteredShards.id("collect_challenge"));
	public static final SoundEvent COLLECT_SECRET_SOUND = SoundEvent.of(ScatteredShards.id("collect_secret"));

	public static final ShardType MISSING = new ShardType(0xFFFFFF, 0xFF00FF, Optional.empty(), Optional.empty(), Optional.empty(), -1);
	public static final Identifier MISSING_ID = ScatteredShards.id("missing");

	public static Identifier createModId(Identifier shardTypeId, String modId) {
		return Identifier.of(modId, shardTypeId.toUnderscoreSeparatedString());
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

	public static Text getDescription(Identifier id) {
		return Text.translatable(id.toTranslationKey("shard_type", "description"));
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

	public NbtCompound toNbt() {
		return (NbtCompound) CODEC.encodeStart(NbtOps.INSTANCE, this).result().orElseThrow();
	}

	public JsonObject toJson() {
		return (JsonObject) CODEC.encodeStart(JsonOps.INSTANCE, this).result().orElseThrow();
	}

	public static ShardType fromNbt(NbtCompound tag) {
		return CODEC.parse(NbtOps.INSTANCE, tag).result().orElseThrow();
	}

	public static ShardType fromJson(JsonObject obj) {
		return CODEC.parse(JsonOps.INSTANCE, obj).result().orElseThrow();
	}

	public static void register() {
		Registry.register(Registries.SOUND_EVENT, COLLECT_VISITOR_SOUND.id(), COLLECT_VISITOR_SOUND);
		Registry.register(Registries.SOUND_EVENT, COLLECT_CHALLENGE_SOUND.id(), COLLECT_CHALLENGE_SOUND);
		Registry.register(Registries.SOUND_EVENT, COLLECT_SECRET_SOUND.id(), COLLECT_SECRET_SOUND);
	}
}
