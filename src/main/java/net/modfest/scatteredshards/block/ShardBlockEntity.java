package net.modfest.scatteredshards.block;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.HolderLookup;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.modfest.scatteredshards.ScatteredShardsContent;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.ShardCollection;
import net.modfest.scatteredshards.api.ShardLibrary;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.api.shard.ShardType;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

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

	public boolean canInteract() {
		return canInteract;
	}

	@Override
	protected void saveAdditional(ValueOutput view) {
		super.saveAdditional(view);

		if (shardId != null) view.putString(SHARD_NBT_KEY, shardId.toString());

		view.putBoolean("CanInteract", this.canInteract);

		CompoundTag glowSettings = new CompoundTag();
		glowSettings.putFloat("size", this.glowSize);
		glowSettings.putFloat("strength", this.glowStrength);
		view.store("Glow", CompoundTag.CODEC, glowSettings);
	}

	@Override
	protected void loadAdditional(ValueInput view) {
		super.loadAdditional(view);

		var shardId = view.getStringOr(SHARD_NBT_KEY, null);
		if (shardId != null) {
			setShardId(Identifier.parse(shardId));
		}

		this.canInteract = view.getBooleanOr("CanInteract", false);

		CompoundTag glowSettings = view.read("Glow", CompoundTag.CODEC).get();
		this.glowSize = glowSettings.getFloat("size").orElse(this.glowSize);
		this.glowStrength = glowSettings.getFloat("strength").orElse(this.glowStrength);
	}

	@Override
	public CompoundTag getUpdateTag(HolderLookup.Provider registryLookup) {
		return saveWithoutMetadata(registryLookup);
	}

	@Override
	public Packet<ClientGamePacketListener> getUpdatePacket() {
		return ClientboundBlockEntityDataPacket.create(this);
	}

	public static void clientTick(Level world, BlockPos pos, BlockState state, BlockEntity entity) {
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
			return (float) (Mth.lerp(tickDelta, this.lastAngle, this.angle) % Math.PI * 2);
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

			final LevelRenderer worldRenderer = Minecraft.getInstance().levelRenderer;
			final RandomSource random = ShardBlockEntity.this.getLevel().getRandom();
			final Vec3 pos = Vec3.atCenterOf(ShardBlockEntity.this.getBlockPos());

			ShardLibrary library = ScatteredShardsAPI.getClientLibrary();

			ShardType shardType = library.shardTypes()
				.get(ShardBlockEntity.this.getShard(library).shardTypeId())
				.orElse(ShardType.MISSING);
			shardType.collectParticle().ifPresent(p -> {
				if (!(p instanceof ParticleOptions particle)) {
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
