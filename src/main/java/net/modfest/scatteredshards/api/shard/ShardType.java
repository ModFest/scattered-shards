package net.modfest.scatteredshards.api.shard;

import java.util.Optional;

import com.google.gson.JsonObject;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.particle.ParticleType;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;

public record ShardType(int textColor, int glowColor, Optional<ParticleType<?>> collectParticle, Optional<SoundEvent> collectSound) {

	public static final SoundEvent COLLECT_VISITOR_SOUND = SoundEvent.createVariableRangeEvent(ScatteredShards.id("collect_visitor"));
	public static final SoundEvent COLLECT_CHALLENGE_SOUND = SoundEvent.createVariableRangeEvent(ScatteredShards.id("collect_challenge"));
	
	public static final ShardType VISITOR = new ShardType(0x6DE851, 0x00FF48, Optional.of(ParticleTypes.TOTEM_OF_UNDYING), Optional.of(COLLECT_VISITOR_SOUND));
	public static final ShardType CHALLENGE = new ShardType(0x5174E8, 0x0026FF, Optional.of(ParticleTypes.GLOW), Optional.of(COLLECT_CHALLENGE_SOUND));
	public static final ShardType SECRET = new ShardType(0xEB4034, 0xFF0088, Optional.of(ParticleTypes.WITCH), Optional.of(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE));
	public static final ShardType MISSING = new ShardType(0xFFFFFF, 0xFF00FF, Optional.empty(), Optional.empty());

	public static final Identifier MISSING_ID = ScatteredShards.id("missing");

	public Identifier getId() {
		return ScatteredShardsAPI.getShardTypes().inverse().get(this);
	}
	
	public Identifier createModId(String modId) {
		return new Identifier(modId, getId().toUnderscoreSeparatedString());
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
	
	public Identifier getMiniFrontTexture() {
		return getTexture("mini_front");
	}
	
	public Identifier getMiniBackTexture() {
		return getTexture("mini_back");
	}

	public Text getDescription() {
		return Text.translatable(getId().toTranslationKey("shard_type", "description"));
	}

	public void write(PacketByteBuf buf) {
		buf.writeInt(textColor);
		buf.writeInt(glowColor);
		buf.writeOptional(collectParticle, (b, t) -> b.writeFromIterable(Registries.PARTICLE_TYPE, t));
		buf.writeOptional(collectSound, (b, t) -> t.toPacket(b));
	}

	public static ShardType read(PacketByteBuf buf) {
		return new ShardType(
				buf.readInt(),
				buf.readInt(),
				buf.readOptional(b -> b.readFromIterable(Registries.PARTICLE_TYPE)),
				buf.readOptional(SoundEvent::fromPacket));
	}

	public static ShardType fromJson(JsonObject obj) {
		int textColor = JsonHelper.getInt(obj, "text_color");

		int glowColor = JsonHelper.getInt(obj, "glow_color");

		String particleId = JsonHelper.getString(obj, "particle", null);
		Optional<ParticleType<?>> particle = Optional.ofNullable(particleId).map(Identifier::new).map(Registries.PARTICLE_TYPE::get);

		String soundId = JsonHelper.getString(obj, "sound", null);
		Optional<SoundEvent> sound = (soundId == null) ? Optional.empty() : Optional.of(Registries.SOUND_EVENT.get(new Identifier(soundId)));
		
		return new ShardType(textColor, glowColor, particle, sound);
	}

	public static void register() {
		Registry.register(Registries.SOUND_EVENT, COLLECT_VISITOR_SOUND.getId(), COLLECT_VISITOR_SOUND);
		Registry.register(Registries.SOUND_EVENT, COLLECT_CHALLENGE_SOUND.getId(), COLLECT_CHALLENGE_SOUND);
		
		ScatteredShardsAPI.registerShardType(ScatteredShards.id("visitor"), VISITOR);
		ScatteredShardsAPI.registerShardType(ScatteredShards.id("challenge"), CHALLENGE);
		ScatteredShardsAPI.registerShardType(ScatteredShards.id("secret"), SECRET);
		ScatteredShardsAPI.registerShardType(MISSING_ID, MISSING);
	}
}
