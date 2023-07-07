package net.modfest.scatteredshards.core.api.shard;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.JsonOps;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtString;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.modfest.scatteredshards.ScatteredShards;

public class Shard {
	
	/** {@see net.minecraft.client.texture.TextureManager#MISSING_IDENTIFIER} */
	private static final Identifier MISSING_TEXTURE_ID = new Identifier("");
	
	protected Identifier shardType;
	protected Text info = Text.literal("");
	protected Text name = Text.literal("");
	protected Text lore = Text.literal("");
	protected Text hint = Text.literal("");
	protected Either<ItemStack, Identifier> icon = Either.right(MISSING_TEXTURE_ID);
	
	public Shard() {}
	
	private Shard(NbtCompound nbt) {
		shardType = new Identifier(nbt.getString("shardType"));
		info = Text.Serializer.fromJson(nbt.getString("info"));
		name = Text.Serializer.fromJson(nbt.getString("name"));
		lore = Text.Serializer.fromJson(nbt.getString("lore"));
		hint = Text.Serializer.fromJson(nbt.getString("hint"));
		NbtElement iconElem = nbt.get("icon");
		if (iconElem instanceof NbtString str) {
			icon = Either.right(new Identifier(str.asString()));
		} else if (iconElem instanceof NbtCompound compound) {
			icon = Either.<ItemStack, Identifier>left(ItemStack.fromNbt(compound));
		} else {
			icon = Either.right(MISSING_TEXTURE_ID);
		}
	}
	
	public Identifier shardType() {
		return shardType;
	}
	
	public Text info() {
		return info;
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
	
	public Shard setShardType(Identifier id) {
		this.shardType = id;
		return this;
	}
	
	public Shard setInfo(Text value) {
		this.info = value;
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
	
	public static Shard fromNbt(NbtCompound nbt) {
		return new Shard(nbt);
	}
	
	public NbtCompound writeNbt(NbtCompound nbt) {
		nbt.putString("shardType", shardType.toString());
		nbt.putString("info", Text.Serializer.toJson(info));
		nbt.putString("name", Text.Serializer.toJson(name));
		nbt.putString("lore", Text.Serializer.toJson(lore));
		nbt.putString("hint", Text.Serializer.toJson(hint));
		
		icon.ifLeft((stack) -> {
			nbt.put("icon", stack.writeNbt(new NbtCompound()));
		});
		icon.ifRight((texture) -> {
			nbt.putString("icon", texture.toString());
		});
		
		return nbt;
	}
	
	public JsonObject toJson() {
		JsonObject result = new JsonObject();
		
		result.add("shard_type", new JsonPrimitive(shardType.toString()));
		result.add("info", new JsonPrimitive(Text.Serializer.toJson(info)));
		result.add("name", new JsonPrimitive(Text.Serializer.toJson(name)));
		result.add("lore", new JsonPrimitive(Text.Serializer.toJson(lore)));
		result.add("hint", new JsonPrimitive(Text.Serializer.toJson(hint)));
		
		icon.ifLeft((stack) -> {
			JsonElement stackTree = ItemStack.CODEC.encode(stack, JsonOps.INSTANCE, new JsonObject())
				.getOrThrow(false, (err) -> ScatteredShards.LOGGER.warn("Couldn't write the icon for a shard: "+err));
			
			result.add("icon", stackTree);
		});
		
		icon.ifRight((texture) -> {
			result.add("icon", new JsonPrimitive(texture.toString()));
		});
		
		return result;
	}
	
	public static Shard fromJson(JsonObject obj) {
		Shard result = new Shard();
		result.shardType = new Identifier(JsonHelper.getString(obj, "shard_type"));
		result.info = Text.Serializer.fromJson(JsonHelper.getString(obj, "info"));
		result.name = Text.Serializer.fromJson(JsonHelper.getString(obj, "name"));
		result.lore = Text.Serializer.fromJson(JsonHelper.getString(obj, "lore"));
		result.hint = Text.Serializer.fromJson(JsonHelper.getString(obj, "hint"));
		
		JsonElement iconElem = obj.get("icon");
		if (iconElem instanceof JsonPrimitive primitive) {
			result.icon = Either.right(new Identifier(primitive.getAsString()));
		} else if (iconElem instanceof JsonObject itemObj) {
			ItemStack item = ItemStack.CODEC.decode(JsonOps.INSTANCE, itemObj).getOrThrow(false, (err) -> ScatteredShards.LOGGER.warn("Couldn't deserialize an ItemStack")).getFirst();
			result.icon = Either.left(item);
		}
		
		return result;
	}
}
