package net.modfest.scatteredshards.core.api.shard;

import org.quiltmc.qsl.registry.api.event.RegistryMonitor;

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
import net.minecraft.registry.Registries;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.modfest.scatteredshards.ScatteredShards;

public class Shard {
	
	/** {@see net.minecraft.client.texture.TextureManager#MISSING_IDENTIFIER} */
	private static final Identifier MISSING_TEXTURE_ID = new Identifier("");
	
	protected Identifier shardType;
	protected Text name = Text.literal("");
	protected Text lore = Text.literal("");
	protected Text hint = Text.literal("");
	protected Either<ItemStack, Identifier> icon = Either.right(MISSING_TEXTURE_ID);
	
	public Shard() {}
	
	private Shard(NbtCompound nbt) {
		shardType = new Identifier(nbt.getString("ShardType"));
		name = Text.Serializer.fromJson(nbt.getString("Name"));
		lore = Text.Serializer.fromJson(nbt.getString("Lore"));
		hint = Text.Serializer.fromJson(nbt.getString("Hint"));
		NbtElement iconElem = nbt.get("Icon");
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
		result.name = Text.Serializer.fromLenientJson(JsonHelper.getString(obj, "name"));
		result.lore = Text.Serializer.fromLenientJson(JsonHelper.getString(obj, "lore"));
		result.hint = Text.Serializer.fromLenientJson(JsonHelper.getString(obj, "hint"));
		
		JsonElement iconElem = obj.get("icon");
		if (iconElem instanceof JsonPrimitive primitive) {
			result.icon = Either.right(new Identifier(primitive.getAsString()));
		} else if (iconElem instanceof JsonObject itemObj) {
			ItemStack item = loadItemStack(itemObj);
			//ItemStack item = ItemStack.CODEC.decode(JsonOps.INSTANCE, itemObj).getOrThrow(false, (err) -> ScatteredShards.LOGGER.warn("Couldn't deserialize an ItemStack")).getFirst();
			result.icon = Either.left(item);
		}
		
		return result;
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
