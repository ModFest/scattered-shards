package net.modfest.scatteredshards.api.shard;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;

import java.util.Optional;

public record ShardDisplaySettings(Optional<ShardIconOffsets> offsets, Optional<ShardTextureSettings> textures) {
	public static final Codec<ShardDisplaySettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.optionalField("offsets", ShardIconOffsets.CODEC, false).forGetter(ShardDisplaySettings::offsets),
		Codec.optionalField("textures", ShardTextureSettings.CODEC, false).forGetter(ShardDisplaySettings::textures)
	).apply(instance, ShardDisplaySettings::new));

	public static final PacketCodec<RegistryByteBuf, ShardDisplaySettings> PACKET_CODEC = PacketCodec.tuple(
		PacketCodecs.optional(ShardIconOffsets.PACKET_CODEC), ShardDisplaySettings::offsets,
		PacketCodecs.optional(ShardTextureSettings.PACKET_CODEC), ShardDisplaySettings::textures,
		ShardDisplaySettings::new
	);

	public static final ShardDisplaySettings DEFAULT = new ShardDisplaySettings(Optional.empty(), Optional.empty());

	public ShardIconOffsets getOffsets() {
		return offsets.orElse(ShardIconOffsets.DEFAULT);
	}

	public ShardTextureSettings getTextureSettings() {
		return textures.orElse(ShardTextureSettings.DEFAULT);
	}
}
