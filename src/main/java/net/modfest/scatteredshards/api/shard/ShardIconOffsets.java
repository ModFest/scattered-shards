package net.modfest.scatteredshards.api.shard;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;

import java.util.Optional;

public record ShardIconOffsets(Optional<Offset> normal, Optional<Offset> mini) {

	public static final Codec<ShardIconOffsets> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Codec.optionalField("normal", Offset.CODEC, false).forGetter(ShardIconOffsets::normal),
		Codec.optionalField("mini", Offset.CODEC, false).forGetter(ShardIconOffsets::mini)
	).apply(instance, ShardIconOffsets::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, ShardIconOffsets> PACKET_CODEC = StreamCodec.composite(
		ByteBufCodecs.optional(Offset.PACKET_CODEC), ShardIconOffsets::normal,
		ByteBufCodecs.optional(Offset.PACKET_CODEC), ShardIconOffsets::mini,
		ShardIconOffsets::new
	);

	public static final ShardIconOffsets DEFAULT = new ShardIconOffsets(Optional.empty(), Optional.empty());

	public record Offset(int up, int left) {

		public static final Codec<Offset> CODEC = RecordCodecBuilder.create(instance -> instance.group(
			Codec.INT.fieldOf("top").forGetter(Offset::up),
			Codec.INT.fieldOf("left").forGetter(Offset::left)
		).apply(instance, Offset::new));

		public static final StreamCodec<RegistryFriendlyByteBuf, Offset> PACKET_CODEC = StreamCodec.composite(
			ByteBufCodecs.INT, Offset::up,
			ByteBufCodecs.INT, Offset::left,
			Offset::new
		);

		public static final Offset DEFAULT = new Offset(4, 4);
		public static final Offset DEFAULT_MINI = new Offset(2, 2);

		public int down() {
			// (32 (card) - 16 (icon)) - up
			return 16 - up;
		}

		public int right() {
			// (24 (card) - 16 (icon)) - left
			return 8 - left;
		}

		public int miniDown() {
			// (16 (card) - 6 (icon)) - up
			return 10 - up;
		}

		public int miniRight() {
			// (12 (card) - 6 (icon)) - left
			return 6 - left;
		}
	}

	public Offset getNormal() {
		return normal.orElse(Offset.DEFAULT);
	}

	public Offset getMini() {
		return mini.orElse(Offset.DEFAULT_MINI);
	}
}
