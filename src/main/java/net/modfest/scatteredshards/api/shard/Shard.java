package net.modfest.scatteredshards.api.shard;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.JsonOps;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import org.quiltmc.loader.api.ModContainer;

import java.util.Objects;
import java.util.stream.Stream;

public class Shard {

	/**
	 * Unfortunately, MISSING_IDENTIFIER still pops an error, so we're using an actual image
	 * @see net.minecraft.client.texture.TextureManager#MISSING_IDENTIFIER
	 */
	private static final Either<ItemStack, Identifier> MISSING_ICON = Either.right(new Identifier("scattered_shards:textures/gui/shards/missing_icon.png"));
	public static final Shard MISSING_SHARD = new Shard(ShardType.MISSING, Text.of("Missing"), Text.of(""), Text.of(""), Text.of("None"), MISSING_ICON);

	protected ShardType shardType;
	protected Text name;
	protected Text lore;
	protected Text hint;
	protected final Text source;
	protected Either<ItemStack, Identifier> icon;

	public Shard(ShardType shardType, Text name, Text lore, Text hint, Text source, Either<ItemStack, Identifier> icon) {
		Stream.of(name, lore, hint, source, icon).forEach(Objects::requireNonNull);
		this.shardType = shardType;
		this.name = name;
		this.lore = lore;
		this.hint = hint;
		this.source = source;
		this.icon = icon;
	}

	public ShardType shardType() {
		return shardType;
	}

	public Text name() {
		return name;
	}

	public Text lore() {
		return lore;
	}

	public Text hint() {
		return hint;
	}

	public Text source() {
		return source;
	}

	public Either<ItemStack, Identifier> icon() {
		return icon;
	}

	public Shard setShardType(ShardType shardType) {
		this.shardType = shardType;
		return this;
	}

	public Shard setName(Text value) {
		this.name = value;
		return this;
	}

	public Shard setLore(Text value) {
		this.lore = value;
		return this;
	}

	public Shard setHint(Text value) {
		this.hint = value;
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

	private static Either<ItemStack, Identifier> iconFromNbt(NbtElement nbt) {
		if (nbt instanceof NbtString str) {
			return Either.right(new Identifier(str.asString()));
		} else if (nbt instanceof NbtCompound compound) {
			return Either.left(ItemStack.fromNbt(compound));
		} else {
			return MISSING_ICON;
		}
	}

	public static Shard fromNbt(NbtCompound nbt) {
		ShardType shardType = ScatteredShardsAPI.getShardTypes().get(new Identifier(nbt.getString("ShardType")));
		Text name = loadText(nbt.getString("Name"));
		Text lore = loadText(nbt.getString("Lore"));
		Text hint = loadText(nbt.getString("Hint"));
		Text source = loadText(nbt.getString("Source"));
		var icon = iconFromNbt(nbt.get("Icon"));
		return new Shard(shardType, name, lore, hint, source, icon);
	}

	public NbtCompound writeNbt(NbtCompound nbt) {
		nbt.putString("ShardType", shardType.getId().toString());
		nbt.putString("Name", Text.Serializer.toJson(name));
		nbt.putString("Lore", Text.Serializer.toJson(lore));
		nbt.putString("Hint", Text.Serializer.toJson(hint));
		nbt.putString("Source", Text.Serializer.toJson(source));

		icon.ifLeft((stack) -> {
			nbt.put("Icon", stack.writeNbt(new NbtCompound()));
		});
		icon.ifRight((texture) -> {
			nbt.putString("Icon", texture.toString());
		});

		return nbt;
	}

	public JsonObject toJson() {
		JsonObject result = new JsonObject();

		result.add("shard_type", new JsonPrimitive(shardType.getId().toString()));
		result.add("name", new JsonPrimitive(Text.Serializer.toJson(name)));
		result.add("lore", new JsonPrimitive(Text.Serializer.toJson(lore)));
		result.add("hint", new JsonPrimitive(Text.Serializer.toJson(hint)));
		result.add("source", new JsonPrimitive(Text.Serializer.toJson(source)));

		icon.ifLeft((stack) -> {
			JsonElement stackTree = ItemStack.CODEC.encode(stack, JsonOps.INSTANCE, new JsonObject())
				.getOrThrow(false, (err) -> ScatteredShards.LOGGER.warn("Couldn't write the icon for a shard: " + err));

			result.add("icon", stackTree);
		});

		icon.ifRight((texture) -> {
			result.add("icon", new JsonPrimitive(texture.toString()));
		});

		return result;
	}

	public void write(PacketByteBuf buf) {
		buf.writeIdentifier(shardType.getId());
		buf.writeString(Text.Serializer.toJson(name));
		buf.writeString(Text.Serializer.toJson(lore));
		buf.writeString(Text.Serializer.toJson(hint));
		buf.writeString(Text.Serializer.toJson(source));
		buf.writeEither(icon, PacketByteBuf::writeItemStack, PacketByteBuf::writeIdentifier);
	}

	public static Shard read(PacketByteBuf buf) {
		ShardType shardType = ScatteredShardsAPI.getShardTypes().get(buf.readIdentifier());
		Text name = loadText(buf.readString());
		Text lore = loadText(buf.readString());
		Text hint = loadText(buf.readString());
		Text source = loadText(buf.readString());
		var icon = buf.readEither(PacketByteBuf::readItemStack, PacketByteBuf::readIdentifier);
		return new Shard(shardType, name, lore, hint, source, icon);
	}

	public static Either<ItemStack, Identifier> iconFromJson(JsonElement element) {
		if (element instanceof JsonPrimitive primitive) {
			return Either.right(new Identifier(primitive.getAsString()));
		} else if (element instanceof JsonObject itemObj) {
			return Either.left(loadItemStack(itemObj));
		} else {
			return MISSING_ICON;
		}
	}

	public static Shard fromJson(JsonObject obj, Text source) {
		ShardType shardType = ScatteredShardsAPI.getShardTypes().get(new Identifier(JsonHelper.getString(obj, "shard_type")));
		Text name = loadText(JsonHelper.getString(obj, "name"));
		Text lore = loadText(JsonHelper.getString(obj, "lore"));
		Text hint = loadText(JsonHelper.getString(obj, "hint"));
		var icon = iconFromJson(obj.get("icon"));
		return new Shard(shardType, name, lore, hint, source, icon);
	}

	@Override
	public String toString() {
		return toJson().toString();
	}

	private static ItemStack loadItemStack(JsonElement elem) {
		if (elem instanceof JsonObject obj) {
			Identifier itemId = new Identifier(obj.get("id").getAsString());
			int count = 1;
			if (obj.has("Count")) {
				count = obj.get("Count").getAsInt();
			}
			ItemStack stack = new ItemStack(Registries.ITEM.get(itemId), count);

			if (obj.has("tag")) {
				NbtCompound tag = NbtCompound.CODEC.decode(JsonOps.INSTANCE, obj.get("tag"))
					.getOrThrow(false, (err) -> ScatteredShards.LOGGER.warn("Couldn't deserialize an ItemStack tag."))
					.getFirst();
				stack.setNbt(tag);
			}

			return stack;
		} else {
			return new ItemStack(Items.AIR);
		}
	}

	public static Text getSourceForNamespace(String namespace) {
		return Text.translatable("shard_pack." + namespace + ".name");
	}

	public static Text getSourceForMod(ModContainer mod) {
		return Text.literal(mod.metadata().name());
	}
	
	private static Text loadText(String s) {
		if (s.startsWith("{")) {
			return Text.Serializer.fromLenientJson(s);
		} else {
			return Text.literal(s);
		}
	}
}
