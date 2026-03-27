package net.modfest.scatteredshards.api.shard;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;

import java.util.Optional;

public record ShardTextureSettings (Optional<Size> size, Optional<Size> miniSize){
	public static final Codec<ShardTextureSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.optionalField("normal", Size.CODEC, false).forGetter(ShardTextureSettings::size),
		Codec.optionalField("mini", Size.CODEC, false).forGetter(ShardTextureSettings::miniSize)
	).apply(instance, ShardTextureSettings::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ShardTextureSettings> PACKET_CODEC = StreamCodec.composite(
		ByteBufCodecs.optional(ShardTextureSettings.Size.PACKET_CODEC), ShardTextureSettings::size,
		ByteBufCodecs.optional(ShardTextureSettings.Size.PACKET_CODEC), ShardTextureSettings::miniSize,
		ShardTextureSettings::new
	);

	public static final ShardTextureSettings DEFAULT = new ShardTextureSettings(Optional.empty(), Optional.empty());

	public record Size(float width, float height) {
		public static final Codec<ShardTextureSettings.Size> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.FLOAT.fieldOf("width").forGetter(ShardTextureSettings.Size::width),
			Codec.FLOAT.fieldOf("height").forGetter(ShardTextureSettings.Size::height)
		).apply(instance, ShardTextureSettings.Size::new));

		public static final StreamCodec<RegistryFriendlyByteBuf, ShardTextureSettings.Size> PACKET_CODEC = StreamCodec.composite(
			ByteBufCodecs.FLOAT, ShardTextureSettings.Size::width,
			ByteBufCodecs.FLOAT, ShardTextureSettings.Size::height,
			ShardTextureSettings.Size::new
		);

		public static final Size DEFAULT = new Size(24, 32);
		public static final Size DEFAULT_MINI = new Size(12, 16);
	}

	public Size getSize() {
		return size.orElse(Size.DEFAULT);
	}

	public Size getMiniSize() {
		return miniSize.orElse(Size.DEFAULT_MINI);
	}
}
