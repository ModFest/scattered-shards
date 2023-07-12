package net.modfest.scatteredshards.core.api.shard;

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

import java.util.Objects;
import java.util.stream.Stream;

public class Shard {

	public static final Identifier VISITOR_TYPE = ScatteredShards.id("visitor");
	public static final Identifier CHALLENGE_TYPE = ScatteredShards.id("challenge");
	public static final Identifier SECRET_TYPE = ScatteredShards.id("secret");

	/**
	 * @see net.minecraft.client.texture.TextureManager#MISSING_IDENTIFIER
	 */
	private static final Either<ItemStack, Identifier> MISSING_ICON = Either.right(new Identifier(""));

	protected Identifier shardType;
	protected Text name;
	protected Text lore;
	protected Text hint;
	protected Either<ItemStack, Identifier> icon;

	protected final Identifier backingTexture;
	protected final Identifier frontTexture;

	public Shard(Identifier shardType, Text name, Text lore, Text hint, Either<ItemStack, Identifier> icon) {
		Stream.of(shardType, name, lore, hint, icon).forEach(Objects::requireNonNull);
		this.shardType = shardType;
		this.name = name;
		this.lore = lore;
		this.hint = hint;
		this.icon = icon;
		backingTexture = getTexture("backing");
		frontTexture = getTexture("front");
	}

	public static Shard empty() {
		return new Shard(VISITOR_TYPE, Text.empty(), Text.empty(), Text.empty(), MISSING_ICON);
	}

	public Identifier shardType() {
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

	public Either<ItemStack, Identifier> icon() {
		return icon;
	}

	public Identifier backingTexture() {
		return backingTexture;
	}

	public Identifier frontTexture() {
		return frontTexture;
	}

	public Shard setShardType(Identifier id) {
		this.shardType = id;
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

	private Identifier getTexture(String type) {
		return shardType.withPath("textures/gui/shards/" + shardType.getPath() + "_" + type + ".png");
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
		Identifier shardType = new Identifier(nbt.getString("ShardType"));
		Text name = Text.Serializer.fromJson(nbt.getString("Name"));
		Text lore = Text.Serializer.fromJson(nbt.getString("Lore"));
		Text hint = Text.Serializer.fromJson(nbt.getString("Hint"));
		var icon = iconFromNbt(nbt.get("Icon"));
		return new Shard(shardType, name, lore, hint, icon);
	}

	public NbtCompound writeNbt(NbtCompound nbt) {
		nbt.putString("ShardType", shardType.toString());
		nbt.putString("Name", Text.Serializer.toJson(name));
		nbt.putString("Lore", Text.Serializer.toJson(lore));
		nbt.putString("Hint", Text.Serializer.toJson(hint));

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

		result.add("shard_type", new JsonPrimitive(shardType.toString()));
		result.add("name", new JsonPrimitive(Text.Serializer.toJson(name)));
		result.add("lore", new JsonPrimitive(Text.Serializer.toJson(lore)));
		result.add("hint", new JsonPrimitive(Text.Serializer.toJson(hint)));

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
		buf.writeIdentifier(shardType);
		buf.writeString(Text.Serializer.toJson(name));
		buf.writeString(Text.Serializer.toJson(lore));
		buf.writeString(Text.Serializer.toJson(hint));
		buf.writeEither(icon, PacketByteBuf::writeItemStack, PacketByteBuf::writeIdentifier);
	}

	public static Shard read(PacketByteBuf buf) {
		Identifier shardType = buf.readIdentifier();
		Text name = Text.Serializer.fromJson(buf.readString());
		Text lore = Text.Serializer.fromJson(buf.readString());
		Text hint = Text.Serializer.fromJson(buf.readString());
		var icon = buf.readEither(PacketByteBuf::readItemStack, PacketByteBuf::readIdentifier);
		return new Shard(shardType, name, lore, hint, icon);
	}

	public static Either<ItemStack, Identifier> iconFromJson(JsonElement element) {
		if (element instanceof JsonPrimitive primitive) {
			return Either.right(new Identifier(primitive.getAsString()));
		} else if (element instanceof JsonObject itemObj) {
			//ItemStack item = ItemStack.CODEC.decode(JsonOps.INSTANCE, itemObj).getOrThrow(false, (err) -> ScatteredShards.LOGGER.warn("Couldn't deserialize an ItemStack")).getFirst();
			return Either.left(loadItemStack(itemObj));
		} else {
			return MISSING_ICON;
		}
	}

	public static Shard fromJson(JsonObject obj) {
		Identifier shardType = new Identifier(JsonHelper.getString(obj, "shard_type"));
		Text name = Text.Serializer.fromLenientJson(JsonHelper.getString(obj, "name"));
		Text lore = Text.Serializer.fromLenientJson(JsonHelper.getString(obj, "lore"));
		Text hint = Text.Serializer.fromLenientJson(JsonHelper.getString(obj, "hint"));
		var icon = iconFromJson(obj.get("icon"));
		return new Shard(shardType, name, lore, hint, icon);
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
}
