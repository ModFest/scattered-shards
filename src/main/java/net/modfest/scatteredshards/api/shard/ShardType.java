package net.modfest.scatteredshards.api.shard;

import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import com.google.gson.JsonObject;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;

public record ShardType(int textColor, Optional<SoundEvent> collectSound) {
	private static final Identifier NULL_SOUND = new Identifier("");

	public static final SoundEvent COLLECT_VISITOR_SOUND = SoundEvent.createVariableRangeEvent(ScatteredShards.id("collect_visitor"));
	public static final SoundEvent COLLECT_CHALLENGE_SOUND = SoundEvent.createVariableRangeEvent(ScatteredShards.id("collect_challenge"));
	
	public static final ShardType VISITOR = new ShardType(0x6DE851, Optional.of(COLLECT_VISITOR_SOUND));
	public static final ShardType CHALLENGE = new ShardType(0x5174E8, Optional.of(COLLECT_CHALLENGE_SOUND));
	public static final ShardType SECRET = new ShardType(0xEB4034, Optional.of(SoundEvents.UI_TOAST_CHALLENGE_COMPLETE));
	public static final ShardType MISSING = new ShardType(0xFFFFFF, Optional.empty());

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
		Identifier sound = collectSound.map(it -> it.getId()).orElse(NULL_SOUND);
		buf.writeIdentifier(sound);
	}

	public static ShardType read(PacketByteBuf buf) {
		int color = buf.readInt();
		Identifier soundId = buf.readIdentifier();
		Optional<SoundEvent> sound = (soundId.equals(NULL_SOUND)) ? Optional.empty() : Optional.of(Registries.SOUND_EVENT.get(soundId));
		
		return new ShardType(color, sound);
	}

	public static ShardType fromJson(JsonObject obj) {
		int color = JsonHelper.getInt(obj, "text_color");
		String soundId = JsonHelper.getString(obj, "sound", null);
		Optional<SoundEvent> sound = (soundId == null) ? Optional.empty() : Optional.of(Registries.SOUND_EVENT.get(new Identifier(soundId)));
		
		return new ShardType(color, sound);
	}

	public static void register() {
		Registry.register(Registries.SOUND_EVENT, COLLECT_VISITOR_SOUND.getId(), COLLECT_VISITOR_SOUND);
		Registry.register(Registries.SOUND_EVENT, COLLECT_CHALLENGE_SOUND.getId(), COLLECT_CHALLENGE_SOUND);
		
		ScatteredShardsAPI.registerShardType(ScatteredShards.id("visitor"), VISITOR);
		ScatteredShardsAPI.registerShardType(ScatteredShards.id("challenge"), CHALLENGE);
		ScatteredShardsAPI.registerShardType(ScatteredShards.id("secret"), SECRET);
		ScatteredShardsAPI.registerShardType(ScatteredShards.id("missing"), MISSING);
	}
}
