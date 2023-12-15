package net.modfest.scatteredshards.networking;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.impl.ScatteredShardsAPIImpl;
import net.modfest.scatteredshards.client.ScatteredShardsClient;
import net.modfest.scatteredshards.component.ScatteredShardsComponents;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.api.shard.ShardType;
import net.modfest.scatteredshards.load.ShardSetLoader;
import net.modfest.scatteredshards.load.ShardTypeLoader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;

public class ScatteredShardsNetworking {

	private static final Identifier RELOAD_DATA = ScatteredShards.id("reload_data");
	private static final Identifier UPDATE_DATA = ScatteredShards.id("update_data");
	private static final Identifier COLLECT_SHARD = ScatteredShards.id("collect_shard");
	private static final Identifier UNCOLLECT_SHARD = ScatteredShards.id("uncollect_shard");
	private static final Identifier MODIFY_SHARD = ScatteredShards.id("modify_shard");
	private static final Identifier MODIFY_SHARD_RESULT = ScatteredShards.id("modify_shard_result");

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

	@Environment(EnvType.CLIENT)
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
		players.forEach(player -> ServerPlayNetworking.send(player, RELOAD_DATA, buf));
	}

	public static void s2cUpdateData(Collection<ServerPlayerEntity> players) {
		PacketByteBuf buf = createDataUpdate(ScatteredShardsAPIImpl.shardTypes, ScatteredShardsAPIImpl.shardSets, ScatteredShardsAPIImpl.shardData);
		players.forEach(player -> ServerPlayNetworking.send(player, UPDATE_DATA, buf));
	}

	public static void s2cCollectShard(ServerPlayerEntity player, Identifier shardId) {
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeIdentifier(shardId);
		ServerPlayNetworking.send(player, COLLECT_SHARD, buf);
	}

	public static void s2cUncollectShard(ServerPlayerEntity player, Identifier shardId) {
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeIdentifier(shardId);
		ServerPlayNetworking.send(player, UNCOLLECT_SHARD, buf);
	}

	@Environment(EnvType.CLIENT)
	public static void c2sModifyShard(Identifier shardId, Shard shard) {
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeIdentifier(shardId);
		shard.write(buf);
		ClientPlayNetworking.send(MODIFY_SHARD, buf);
	}

	public static void s2cModifyShardResult(ServerPlayerEntity player, Identifier shardId, boolean success) {
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeIdentifier(shardId);
		buf.writeBoolean(success);
		ServerPlayNetworking.send(player, MODIFY_SHARD_RESULT, buf);
	}

	@Environment(EnvType.CLIENT)
	public static void registerClient() {
		ClientPlayNetworking.registerGlobalReceiver(RELOAD_DATA, (client, handler, buf, responseSender) -> {
			updateData(client, buf, ShardTypeLoader.MAP, ShardSetLoader.BY_SHARD_SET, ShardSetLoader.BY_ID);
		});
		ClientPlayNetworking.registerGlobalReceiver(UPDATE_DATA, (client, handler, buf, responseSender) -> {
			updateData(client, buf, ScatteredShardsAPIImpl.shardTypes, ScatteredShardsAPIImpl.shardSets, ScatteredShardsAPIImpl.shardData);
		});
		ClientPlayNetworking.registerGlobalReceiver(COLLECT_SHARD, (client, handler, buf, responseSender) -> {
			final Identifier shardId = buf.readIdentifier();

			client.execute(() -> {
				ScatteredShardsClient.triggerShardCollectAnimation(shardId);
				ScatteredShardsComponents.COLLECTION.get(client.player).addShard(shardId);
			});
		});
		ClientPlayNetworking.registerGlobalReceiver(UNCOLLECT_SHARD, (client, handler, buf, responseSender) -> {
			final Identifier shardId = buf.readIdentifier();

			client.execute(() -> {
				ScatteredShardsComponents.COLLECTION.get(client.player).removeShard(shardId);
			});
		});
		ClientPlayNetworking.registerGlobalReceiver(MODIFY_SHARD_RESULT, (client, handler, buf, responseSender) -> {
			final Identifier shardId = buf.readIdentifier();
			final boolean success = buf.readBoolean();

			client.execute(() -> {
				ScatteredShardsClient.triggerShardModificationToast(shardId, success);
			});
		});
	}

	public static void register() {
		ServerPlayNetworking.registerGlobalReceiver(MODIFY_SHARD, (server, player, handler, buf, responseSender) -> {
			final Identifier shardId = buf.readIdentifier();
			final Shard shard = Shard.read(buf);
			server.execute(() -> {
				boolean success = Permissions.check(player, ScatteredShardsAPI.MODIFY_SHARD_PERMISSION, 1);
				if (success) {
					ScatteredShardsComponents.getShardLibrary(player.getWorld()).modifyShard(shardId, shard, player.getWorld(), player);
				}
				s2cModifyShardResult(player, shardId, success);
			});
		});
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			s2cUpdateData(Collections.singletonList(handler.player));
		});
		ServerLifecycleEvents.END_DATA_PACK_RELOAD.register((server, manager, success) -> {
			if (server != null) {
				ScatteredShardsNetworking.s2cReloadData(server.getPlayerManager().getPlayerList());
			}
		});
	}
}
