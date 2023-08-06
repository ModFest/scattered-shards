package net.modfest.scatteredshards.block;

import java.util.Objects;

import net.modfest.scatteredshards.ScatteredShardsContent;
import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.component.ScatteredShardsComponents;

public class ShardBlockEntity extends BlockEntity {
	public static final String SHARD_NBT_KEY = "Shard";

	@Nullable
	protected Identifier shardId;

	@Nullable
	protected Shard shard;

	public ShardBlockEntity(BlockPos pos, BlockState state) {
		super(ScatteredShardsContent.SHARD_BLOCKENTITY, pos, state);

	}

	@Nullable
	public Identifier getShardId() {
		return shardId;
	}

	@Nullable
	public Shard getShard() {
		if (shard == null && world != null) {
			if (shardId == null) {
				return Shard.MISSING_SHARD;
			}
			shard = ScatteredShardsComponents.getShardLibrary(world).getShard(shardId);
		}
		return shard;
	}

	public void setShardId(Identifier id) {
		Objects.requireNonNull(id);
		this.shardId = id;
		this.shard = null;
	}

	@Override
	protected void writeNbt(NbtCompound nbt) {
		super.writeNbt(nbt);
		if (shardId!=null) nbt.putString(SHARD_NBT_KEY, shardId.toString());
	}

	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);
		if (nbt.contains(SHARD_NBT_KEY, NbtElement.STRING_TYPE)) {
			setShardId(new Identifier(nbt.getString(SHARD_NBT_KEY)));
		}
	}
	
	@Override
	public NbtCompound toSyncedNbt() {
		return this.toNbt();
	}
	
	@Override
	public Packet<ClientPlayPacketListener> toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.of(this);
	}
}
