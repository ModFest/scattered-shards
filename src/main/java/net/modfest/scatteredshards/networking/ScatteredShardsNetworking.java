package net.modfest.scatteredshards.networking;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.impl.ScatteredShardsAPIImpl;
import net.modfest.scatteredshards.client.ScatteredShardsClient;
import net.modfest.scatteredshards.component.ScatteredShardsComponents;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.api.shard.ShardType;
import net.modfest.scatteredshards.load.ShardSetLoader;
import net.modfest.scatteredshards.load.ShardTypeLoader;
import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.ServerPlayConnectionEvents;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;
import org.quiltmc.qsl.resource.loader.api.ResourceLoaderEvents;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class ScatteredShardsNetworking {

	private static final Identifier RELOAD_DATA = ScatteredShards.id("reload_data");
	private static final Identifier UPDATE_DATA = ScatteredShards.id("update_data");
	private static final Identifier COLLECT_SHARD = ScatteredShards.id("collect_shard");

	private static PacketByteBuf createDataUpdate(Map<Identifier, ShardType> shardTypes, Multimap<Identifier, Shard> shardSets, Map<Identifier, Shard> shardData) {
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeMap(shardTypes, PacketByteBuf::writeIdentifier, (typeBuf, type) -> type.write(typeBuf));
		buf.writeMap(shardSets.asMap(), PacketByteBuf::writeIdentifier, (setBuf, shards) -> {
			setBuf.writeCollection(shards, (shardBuf, shard) -> shard.write(shardBuf));
		});
		buf.writeMap(shardData, PacketByteBuf::writeIdentifier, (shardBuf, shard) -> shard.write(shardBuf));
		return buf;
	}

	private static Map<Identifier, ShardType> readShardTypes(PacketByteBuf buf) {
		return buf.readMap(PacketByteBuf::readIdentifier, ShardType::read);
	}

	private static Multimap<Identifier, Shard> readShardSets(PacketByteBuf buf) {
		Multimap<Identifier, Shard> bySet = MultimapBuilder.hashKeys().arrayListValues(3).build();
		var setMap = buf.readMap(PacketByteBuf::readIdentifier, setBuf -> {
			return setBuf.readCollection(ArrayList::new, Shard::read);
		});
		for (var entry : setMap.entrySet()) {
			bySet.putAll(entry.getKey(), entry.getValue());
		}
		return bySet;
	}

	private static Map<Identifier, Shard> readShardData(PacketByteBuf buf) {
		return buf.readMap(PacketByteBuf::readIdentifier, Shard::read);
	}

	@ClientOnly
	private static void updateData(MinecraftClient client, PacketByteBuf buf, Map<Identifier, ShardType> shardTypeMap, Multimap<Identifier, Shard> shardSetMap, Map<Identifier, Shard> shardDataMap) {
		final var shardTypes = readShardTypes(buf);
		final var shardSets = readShardSets(buf);
		final var shardData = readShardData(buf);
		client.execute(() -> {
			shardTypeMap.putAll(shardTypes);
			shardSetMap.putAll(shardSets);
			shardDataMap.putAll(shardData);
		});
	}

	public static void s2cReloadData(Collection<ServerPlayerEntity> players) {
		PacketByteBuf buf = createDataUpdate(ShardTypeLoader.MAP, ShardSetLoader.BY_SHARD_SET, ShardSetLoader.BY_ID);
		ServerPlayNetworking.send(players, RELOAD_DATA, buf);
	}

	public static void s2cUpdateData(Collection<ServerPlayerEntity> players) {
		PacketByteBuf buf = createDataUpdate(ScatteredShardsAPIImpl.shardTypes, ScatteredShardsAPIImpl.shardSets, ScatteredShardsAPIImpl.shardData);
		ServerPlayNetworking.send(players, UPDATE_DATA, buf);
	}

	public static void s2cCollectShard(ServerPlayerEntity player, Identifier shardId) {
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeString(shardId.toString());
		ServerPlayNetworking.send(player, COLLECT_SHARD, buf);
	}

	@ClientOnly
	public static void registerClient() {
		ClientPlayNetworking.registerGlobalReceiver(RELOAD_DATA, (client, handler, buf, responseSender) -> {
			updateData(client, buf, ShardTypeLoader.MAP, ShardSetLoader.BY_SHARD_SET, ShardSetLoader.BY_ID);
		});
		ClientPlayNetworking.registerGlobalReceiver(UPDATE_DATA, (client, handler, buf, responseSender) -> {
			updateData(client, buf, ScatteredShardsAPIImpl.shardTypes, ScatteredShardsAPIImpl.shardSets, ScatteredShardsAPIImpl.shardData);
		});
		ClientPlayNetworking.registerGlobalReceiver(COLLECT_SHARD, (client, handler, buf, responseSender) -> {
			final Identifier shardId = new Identifier(buf.readString());

			client.execute(() -> {
				ScatteredShardsClient.triggerShardCollectAnimation(shardId);
				ScatteredShardsComponents.COLLECTION.get(client.player).addShard(shardId);
			});
		});
	}

	public static void register() {
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			s2cUpdateData(Collections.singletonList(handler.player));
		});
		ResourceLoaderEvents.END_DATA_PACK_RELOAD.register(context -> {
			if (context.server() != null) {
				ScatteredShardsNetworking.s2cReloadData(context.server().getPlayerManager().getPlayerList());
			}
		});
	}
}
