package net.modfest.scatteredshards.api.shard;

import java.util.Optional;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.particle.ParticleType;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.impl.ColorCodec;

public record ShardType(int textColor, int glowColor, Optional<ParticleType<?>> collectParticle, Optional<SoundEvent> collectSound, int listOrder) {
	
	public static final Codec<ShardType> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ColorCodec.CODEC.fieldOf("text_color").forGetter(ShardType::textColor),
			ColorCodec.CODEC.fieldOf("glow_color").forGetter(ShardType::glowColor),
			Codec.optionalField("collect_particle", Registries.PARTICLE_TYPE.getCodec()).forGetter(ShardType::collectParticle),
			Codec.optionalField("collect_sound", SoundEvent.CODEC).forGetter(ShardType::collectSound),
			Codec.INT.fieldOf("list_order").forGetter(ShardType::listOrder)
		).apply(instance, ShardType::new));
	
	public static final SoundEvent COLLECT_VISITOR_SOUND = SoundEvent.of(ScatteredShards.id("collect_visitor"));
	public static final SoundEvent COLLECT_CHALLENGE_SOUND = SoundEvent.of(ScatteredShards.id("collect_challenge"));
	public static final SoundEvent COLLECT_SECRET_SOUND = SoundEvent.of(ScatteredShards.id("collect_secret"));

	public static final ShardType MISSING = new ShardType(0xFFFFFF, 0xFF00FF, Optional.empty(), Optional.empty(), -1);
	public static final Identifier MISSING_ID = ScatteredShards.id("missing");
	
	public static Identifier createModId(Identifier shardTypeId, String modId) {
		return new Identifier(modId, shardTypeId.toUnderscoreSeparatedString());
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
		Registry.register(Registries.SOUND_EVENT, COLLECT_VISITOR_SOUND.getId(), COLLECT_VISITOR_SOUND);
		Registry.register(Registries.SOUND_EVENT, COLLECT_CHALLENGE_SOUND.getId(), COLLECT_CHALLENGE_SOUND);
		Registry.register(Registries.SOUND_EVENT, COLLECT_SECRET_SOUND.getId(), COLLECT_SECRET_SOUND);
	}
}
