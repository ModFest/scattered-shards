package net.modfest.scatteredshards.block;

import java.util.Objects;

import org.jetbrains.annotations.Nullable;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.network.listener.ClientPlayPacketListener;
import net.minecraft.network.packet.Packet;
import net.minecraft.network.packet.s2c.play.BlockEntityUpdateS2CPacket;
import net.minecraft.particle.DefaultParticleType;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.modfest.scatteredshards.ScatteredShardsContent;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.ShardCollection;
import net.modfest.scatteredshards.api.ShardLibrary;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.api.shard.ShardType;

public class ShardBlockEntity extends BlockEntity {
	public static final String SHARD_NBT_KEY = "Shard";

	@Nullable
	protected Identifier shardId;

	@Nullable
	protected Shard shard;

	protected float glowSize = 0.5f;
	protected float glowStrength = 0.5f;
	protected boolean canInteract = false;

	private Animations animations = null;

	public ShardBlockEntity(BlockPos pos, BlockState state) {
		super(ScatteredShardsContent.SHARD_BLOCKENTITY, pos, state);

	}

	@Nullable
	public Identifier getShardId() {
		return shardId;
	}

	@Nullable
	public Shard getShard(ShardLibrary library) {
		if (shardId == null) return Shard.MISSING_SHARD;

		return library.shards().get(shardId).orElse(Shard.MISSING_SHARD);
	}

	public void setShardId(Identifier id) {
		Objects.requireNonNull(id);
		this.shardId = id;
		this.shard = null;
	}

	public Animations getAnimations() {
		if (this.animations == null) {
			this.animations = new Animations();
		}

		return this.animations;
	}

	public float getGlowSize() {
		return glowSize;
	}

	public float getGlowStrength() {
		return glowStrength;
	}

	@Override
	protected void writeNbt(NbtCompound nbt) {
		super.writeNbt(nbt);
		if (shardId!=null) nbt.putString(SHARD_NBT_KEY, shardId.toString());

		nbt.putBoolean("CanInteract", this.canInteract);

		NbtCompound glowSettings = new NbtCompound();
		glowSettings.putFloat("size", this.glowSize);
		glowSettings.putFloat("strength", this.glowStrength);
		nbt.put("Glow", glowSettings);
	}

	@Override
	public void readNbt(NbtCompound nbt) {
		super.readNbt(nbt);
		if (nbt.contains(SHARD_NBT_KEY, NbtElement.STRING_TYPE)) {
			setShardId(new Identifier(nbt.getString(SHARD_NBT_KEY)));
		}

		this.canInteract = nbt.getBoolean("CanInteract");

		NbtCompound glowSettings = nbt.getCompound("Glow");
		this.glowSize = glowSettings.getFloat("size");
		this.glowStrength = glowSettings.getFloat("strength");
	}

	@Override
	public NbtCompound toInitialChunkDataNbt() {
		return createNbt();
	}
	
	@Override
	public Packet<ClientPlayPacketListener> toUpdatePacket() {
		return BlockEntityUpdateS2CPacket.create(this);
	}

	public static void clientTick(World world, BlockPos pos, BlockState state, BlockEntity entity) {
		if (entity instanceof ShardBlockEntity self) {
			self.getAnimations().tick();
		}
	}

	public class Animations {
		public static final float UNCOLLECTED_SPIN_SPEED = 1 / 16f; // Radians per tick
		public static final float COLLECTED_SPIN_SPEED = 1 / 32f; // Radians per tick
		public static final float ON_COLLECT_SPIN_SPEED = 1f; // Radians per tick

		public static final float SPIN_DAMPER = 0.94f; // Spin speed is multiplied by this to slow down

		private boolean collected = true;
		private float angle = 0;
		private float lastAngle = 0;
		private float spinSpeed = UNCOLLECTED_SPIN_SPEED;

		public float getAngle(float tickDelta) {
			return (float) (MathHelper.lerp(tickDelta, this.lastAngle, this.angle) % Math.PI*2);
		}

		public boolean collected() {
			return collected;
		}

		public void tick() {
			Identifier shardId = ShardBlockEntity.this.getShardId();

			boolean wasCollected = this.collected;
			ShardCollection shards = ScatteredShardsAPI.getClientCollection();

			this.collected = shards.contains(shardId);

			if (!wasCollected && this.collected) {
				playCollectAnimation();
			}

			this.lastAngle = this.angle;
			this.angle = (this.angle + spinSpeed);

			float minSpinSpeed = this.collected ? COLLECTED_SPIN_SPEED : UNCOLLECTED_SPIN_SPEED;

			this.spinSpeed = Math.max(minSpinSpeed, this.spinSpeed * SPIN_DAMPER);
		}

		public void playCollectAnimation() {
			this.spinSpeed = ON_COLLECT_SPIN_SPEED;

			@SuppressWarnings("resource")
			final WorldRenderer worldRenderer = MinecraftClient.getInstance().worldRenderer;
			final Random random = ShardBlockEntity.this.getWorld().getRandom();
			final Vec3d pos = Vec3d.ofCenter(ShardBlockEntity.this.getPos());
			
			ShardLibrary library = ScatteredShardsAPI.getClientLibrary();
			
			ShardType shardType = library.shardTypes()
					.get(ShardBlockEntity.this.getShard(library).shardTypeId())
					.orElse(ShardType.MISSING);
			shardType.collectParticle().ifPresent(p -> {
				if (!(p instanceof DefaultParticleType particle)) {
					return;
				}

				for (int i = 0; i < 12; i++) {
					double angle = random.nextDouble() * 2 * Math.PI;
					double speed = 0.5 + random.nextDouble();

					worldRenderer.addParticle(
							particle, false,
							pos.x, pos.y, pos.z,
							Math.sin(angle) * speed, 0, Math.cos(angle) * speed
					);
				}
			});
		}
	}
}
