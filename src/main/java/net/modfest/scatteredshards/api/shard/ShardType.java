package net.modfest.scatteredshards.api.shard;

import java.util.Optional;

import com.google.gson.JsonObject;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtOps;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.dynamic.Codecs;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;

public record ShardType(int textColor, int glowColor, Optional<ParticleType<?>> collectParticle, Optional<SoundEvent> collectSound) {
	public static final Codec<Integer> ANY_INT = Codecs.rangedInt(Integer.MIN_VALUE, Integer.MAX_VALUE);
	public static  final Codec<ParticleType<?>> PARTICLE_TYPE_CODEC = Registries.PARTICLE_TYPE.getCodec();
	//public static final Codec<ParticleType<?>> PARTICLE_TYPE_CODEC =
	//	Identifier.CODEC.xmap(
	//		Registries.PARTICLE_TYPE::get,
	//		Registries.PARTICLE_TYPE::getId
	//	);
	
	public static final Codec<ShardType> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			ANY_INT.fieldOf("textColor").forGetter(ShardType::textColor),
			ANY_INT.fieldOf("glowColor").forGetter(ShardType::glowColor),
			Codec.optionalField("collectParticle", PARTICLE_TYPE_CODEC).forGetter(ShardType::collectParticle),
			Codec.optionalField("collectSound", SoundEvent.CODEC).forGetter(ShardType::collectSound)
		).apply(instance, ShardType::new));
			
	/*
	private static final String TEXT_COLOR_KEY = "TextColor";
	private static final String GLOW_COLOR_KEY = "GlowColor";
	private static final String COLLECT_PARTICLE_KEY = "CollectParticle";
	private static final String COLLECT_SOUND_KEY = "CollectSound";
	
	private static final String TEXT_COLOR_JSON_KEY = "text_color";
	private static final String GLOW_COLOR_JSON_KEY = "glow_color";
	private static final String COLLECT_PARTICLE_JSON_KEY = "particle";
	private static final String COLLECT_SOUND_JSON_KEY = "sound";*/
	
	public static final SoundEvent COLLECT_VISITOR_SOUND = SoundEvent.of(ScatteredShards.id("collect_visitor"));
	public static final SoundEvent COLLECT_CHALLENGE_SOUND = SoundEvent.of(ScatteredShards.id("collect_challenge"));
	public static final SoundEvent COLLECT_SECRET_SOUND = SoundEvent.of(ScatteredShards.id("collect_secret"));
	
	public static final ShardType VISITOR = new ShardType(0x6DE851, 0x00FF48, Optional.of(ParticleTypes.TOTEM_OF_UNDYING), Optional.of(COLLECT_VISITOR_SOUND));
	public static final ShardType CHALLENGE = new ShardType(0x5174E8, 0x0026FF, Optional.of(ParticleTypes.GLOW), Optional.of(COLLECT_CHALLENGE_SOUND));
	public static final ShardType SECRET = new ShardType(0xEB4034, 0xFF0088, Optional.of(ParticleTypes.WITCH), Optional.of(COLLECT_SECRET_SOUND));
	public static final ShardType MISSING = new ShardType(0xFFFFFF, 0xFF00FF, Optional.empty(), Optional.empty());

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
	
	public static Identifier getMiniBackTexture(Identifier id) {
		return getTexture(id, "mini_back");
	}

	public static Text getDescription(Identifier id) {
		return Text.translatable(id.toTranslationKey("shard_type", "description"));
	}
	/*
	public void write(PacketByteBuf buf) {
		buf.writeInt(textColor);
		buf.writeInt(glowColor);
		buf.writeOptional(collectParticle, (b, t) -> b.writeRegistryValue(Registries.PARTICLE_TYPE, t));
		buf.writeOptional(collectSound, (b, t) -> t.writeBuf(b));
	}

	public static ShardType read(PacketByteBuf buf) {
		return new ShardType(
				buf.readInt(),
				buf.readInt(),
				buf.readOptional(b -> b.readRegistryValue(Registries.PARTICLE_TYPE)),
				buf.readOptional(SoundEvent::fromBuf));
	}*/
	
	public NbtCompound toNbt() {
		return (NbtCompound) CODEC.encodeStart(NbtOps.INSTANCE, this).result().orElseThrow();
		/*
		compound.putInt(TEXT_COLOR_KEY, textColor);
		compound.putInt(GLOW_COLOR_KEY, glowColor);
		collectParticle.ifPresent((it) -> {
			compound.putString(COLLECT_PARTICLE_KEY, Registries.PARTICLE_TYPE.getId(it).toString());
		});
		collectSound.ifPresent((it) -> {
			compound.putString(COLLECT_SOUND_KEY, Registries.SOUND_EVENT.getId(it).toString());
		});
		
		return compound;*/
	}
	
	public JsonObject toJson() {
		return (JsonObject) CODEC.encodeStart(JsonOps.INSTANCE, this).result().orElseThrow();
	}
	
	public static ShardType fromNbt(NbtCompound tag) {
		return CODEC.parse(NbtOps.INSTANCE, tag).result().orElseThrow();
		/*
		int shardColor = tag.getInt(TEXT_COLOR_KEY);
		int glowColor = tag.getInt(GLOW_COLOR_KEY);
		Optional<ParticleType<?>> collectParticle = 
				Optional.of(tag.getString(COLLECT_PARTICLE_KEY))
				.filter(it->!it.isBlank())
				.map(Identifier::new)
				.map(Registries.PARTICLE_TYPE::get);
		Optional<SoundEvent> collectSound =
				Optional.of(tag.getString(COLLECT_SOUND_KEY))
				.filter(it->!it.isBlank())
				.map(Identifier::new)
				.map(Registries.SOUND_EVENT::get);
		
		return new ShardType(shardColor, glowColor, collectParticle, collectSound);*/
	}
	
	public static ShardType fromJson(JsonObject obj) {
		return CODEC.parse(JsonOps.INSTANCE, obj).result().orElseThrow();
		/*
		int textColor = JsonHelper.getInt(obj, TEXT_COLOR_JSON_KEY);
		int glowColor = JsonHelper.getInt(obj, GLOW_COLOR_JSON_KEY);

		String particleId = JsonHelper.getString(obj, COLLECT_PARTICLE_JSON_KEY, null);
		Optional<ParticleType<?>> particle = Optional.ofNullable(particleId).map(Identifier::new).map(Registries.PARTICLE_TYPE::get);

		String soundId = JsonHelper.getString(obj, COLLECT_SOUND_JSON_KEY, null);
		Optional<SoundEvent> sound = (soundId == null) ? Optional.empty() : Optional.of(Registries.SOUND_EVENT.get(new Identifier(soundId)));
		
		return new ShardType(textColor, glowColor, particle, sound);*/
	}

	public static void register() {
		Registry.register(Registries.SOUND_EVENT, COLLECT_VISITOR_SOUND.getId(), COLLECT_VISITOR_SOUND);
		Registry.register(Registries.SOUND_EVENT, COLLECT_CHALLENGE_SOUND.getId(), COLLECT_CHALLENGE_SOUND);
		Registry.register(Registries.SOUND_EVENT, COLLECT_SECRET_SOUND.getId(), COLLECT_SECRET_SOUND);
		
		var shardTypes = ScatteredShardsAPI.getServerLibrary().shardTypes();
		shardTypes.put(ScatteredShards.id("visitor"), VISITOR);
		shardTypes.put(ScatteredShards.id("challenge"), CHALLENGE);
		shardTypes.put(ScatteredShards.id("secret"), SECRET);
		shardTypes.put(MISSING_ID, MISSING);
	}
}
