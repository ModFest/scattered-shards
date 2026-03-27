package net.modfest.scatteredshards.api.shard;

import com.google.gson.JsonObject;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.ModContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.resources.Identifier;
import net.modfest.scatteredshards.ScatteredShards;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

public class Shard {
	public static final Codec<Either<ItemStack, Identifier>> ICON_CODEC = Codec.either(ItemStack.CODEC, Identifier.CODEC);

	public static final Codec<Shard> CODEC = RecordCodecBuilder.create(instance -> instance.group(
		Identifier.CODEC.fieldOf("shard_type_id").forGetter(Shard::shardTypeId),
		ComponentSerialization.CODEC.fieldOf("name").forGetter(Shard::name),
		ComponentSerialization.CODEC.fieldOf("lore").forGetter(Shard::lore),
		ComponentSerialization.CODEC.fieldOf("hint").forGetter(Shard::hint),
		Identifier.CODEC.fieldOf("source_id").forGetter(Shard::sourceId),
		ICON_CODEC.fieldOf("icon").forGetter(Shard::icon)
	).apply(instance, Shard::new));

	public static final StreamCodec<RegistryFriendlyByteBuf, Shard> PACKET_CODEC = ByteBufCodecs.fromCodec(CODEC).cast();

	public static final Identifier MISSING_ICON_ID = ScatteredShards.id("textures/gui/shards/missing_icon.png");
	public static final Either<ItemStack, Identifier> MISSING_ICON = Either.right(MISSING_ICON_ID);
	public static final Identifier MISSING_SHARD_SOURCE = ScatteredShards.id("missing");
	public static final Shard MISSING_SHARD = new Shard(ShardType.MISSING_ID, Component.nullToEmpty("Missing"), Component.nullToEmpty(""), Component.nullToEmpty(""), MISSING_SHARD_SOURCE, MISSING_ICON);

	protected Identifier shardTypeId;
	protected Component name;
	protected Component lore;
	protected Component hint;
	protected Identifier sourceId;
	protected Either<ItemStack, Identifier> icon;

	public Shard(Identifier shardTypeId, Component name, Component lore, Component hint, Identifier sourceId, Either<ItemStack, Identifier> icon) {
		Stream.of(name, lore, hint, icon).forEach(Objects::requireNonNull);
		this.shardTypeId = shardTypeId;
		this.name = name;
		this.lore = lore;
		this.hint = hint;
		this.sourceId = sourceId;
		this.icon = icon;
	}

	public Identifier shardTypeId() {
		return shardTypeId;
	}

	public Component name() {
		return name;
	}

	public Component lore() {
		return lore;
	}

	public Component hint() {
		return hint;
	}

	public Identifier sourceId() {
		return sourceId;
	}

	public Either<ItemStack, Identifier> icon() {
		return icon;
	}

	public Shard setShardType(Identifier shardTypeId) {
		this.shardTypeId = shardTypeId;
		return this;
	}

	public Shard setName(Component value) {
		this.name = value;
		return this;
	}

	public Shard setLore(Component value) {
		this.lore = value;
		return this;
	}

	public Shard setHint(Component value) {
		this.hint = value;
		return this;
	}

	public Shard setIcon(Either<ItemStack, Identifier> icon) {
		this.icon = icon;
		return this;
	}

	public Shard setIcon(ItemStack itemValue) {
		this.icon = Either.left(itemValue);
		return this;
	}

	public Shard setIcon(Identifier textureValue) {
		this.icon = Either.right(textureValue);
		return this;
	}

	public Shard setSourceId(Identifier id) {
		this.sourceId = id;
		return this;
	}

	public static Shard fromNbt(CompoundTag nbt) {
		return CODEC.parse(NbtOps.INSTANCE, nbt).result().orElseThrow();
	}

	public CompoundTag toNbt() {
		return (CompoundTag) CODEC.encodeStart(NbtOps.INSTANCE, this).result().orElseThrow();
	}

	public JsonObject toJson() {
		return (JsonObject) CODEC.encodeStart(JsonOps.INSTANCE, this).result().orElseThrow();
	}

	public Shard copy() {
		Either<ItemStack, Identifier> icon = icon().mapBoth(stack -> stack, id -> id);
		return new Shard(shardTypeId, name.copy(), lore.copy(), hint.copy(), sourceId, icon);
	}

	@Override
	public String toString() {
		return toJson().toString();
	}

	public static Shard emptyOfType(Identifier id) {
		return MISSING_SHARD.copy().setShardType(id).setName(Component.nullToEmpty(""));
	}

	public static Component getSourceForMod(ModContainer mod) {
		return Component.literal(mod.getMetadata().getName());
	}

	public static Optional<Component> getSourceForModId(String modId) {
		return FabricLoader.getInstance().getModContainer(modId).map(Shard::getSourceForMod);
	}

	public static Component getSourceForSourceId(Identifier id) {
		if (!id.getPath().equals("shard_pack")) {
			return Component.translatable("shard_pack." + id.getNamespace() + "." + id.getPath() + ".name");
		}

		return getSourceForModId(id.getNamespace())
			.orElse(Component.translatable("shard_pack." + id.getNamespace() + ".name"));
	}
}
