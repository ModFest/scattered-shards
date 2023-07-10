package net.modfest.scatteredshards.networking;

import com.google.common.collect.Multimap;
import com.google.common.collect.MultimapBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.impl.ScatteredShardsAPIImpl;
import net.modfest.scatteredshards.core.api.shard.Shard;
import net.modfest.scatteredshards.load.ShardSetLoader;
import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.networking.api.PacketByteBufs;
import org.quiltmc.qsl.networking.api.ServerPlayConnectionEvents;
import org.quiltmc.qsl.networking.api.ServerPlayNetworking;
import org.quiltmc.qsl.networking.api.client.ClientPlayNetworking;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.function.Consumer;

public class ScatteredShardsNetworking {

	private static final Identifier RELOAD_SHARDS = ScatteredShards.id("reload_shards");
	private static final Identifier UPDATE_SHARDS = ScatteredShards.id("update_shards");

	private static PacketByteBuf createMapUpdate(Multimap<Identifier, Shard> bySet, Map<Identifier, Shard> byId) {
		PacketByteBuf buf = PacketByteBufs.create();
		buf.writeMap(bySet.asMap(), PacketByteBuf::writeIdentifier, (setBuf, shards) -> {
			setBuf.writeCollection(shards, (shardBuf, shard) -> shard.write(shardBuf));
		});
		buf.writeMap(byId, PacketByteBuf::writeIdentifier, (shardBuf, shard) -> shard.write(shardBuf));
		return buf;
	}

	private static Multimap<Identifier, Shard> readBySet(PacketByteBuf buf) {
		Multimap<Identifier, Shard> bySet = MultimapBuilder.hashKeys().arrayListValues(3).build();
		var setMap = buf.readMap(PacketByteBuf::readIdentifier, setBuf -> {
			return setBuf.readCollection(ArrayList::new, Shard::read);
		});
		for (var entry : setMap.entrySet()) {
			bySet.putAll(entry.getKey(), entry.getValue());
		}
		return bySet;
	}

	private static Map<Identifier, Shard> readById(PacketByteBuf buf) {
		return buf.readMap(PacketByteBuf::readIdentifier, Shard::read);
	}

	@ClientOnly
	private static void readMapUpdate(MinecraftClient client, PacketByteBuf buf, Consumer<Multimap<Identifier, Shard>> bySetCallback, Consumer<Map<Identifier, Shard>> byIdCallback) {
		final var bySet = readBySet(buf);
		final var byId = readById(buf);
		client.execute(() -> {
			bySetCallback.accept(bySet);
			byIdCallback.accept(byId);
		});
	}

	public static void s2cReloadShards(Collection<ServerPlayerEntity> players) {
		PacketByteBuf buf = createMapUpdate(ShardSetLoader.BY_SHARD_SET, ShardSetLoader.BY_ID);
		ServerPlayNetworking.send(players, RELOAD_SHARDS, buf);
	}

	public static void s2cUpdateShards(Collection<ServerPlayerEntity> players) {
		PacketByteBuf buf = createMapUpdate(ScatteredShardsAPIImpl.shardSets, ScatteredShardsAPIImpl.shardData);
		ServerPlayNetworking.send(players, UPDATE_SHARDS, buf);
	}

	@ClientOnly
	public static void registerClient() {
		ClientPlayNetworking.registerGlobalReceiver(RELOAD_SHARDS, (client, handler, buf, responseSender) -> {
			readMapUpdate(client, buf, ShardSetLoader.BY_SHARD_SET::putAll, ShardSetLoader.BY_ID::putAll);
		});
		ClientPlayNetworking.registerGlobalReceiver(UPDATE_SHARDS, (client, handler, buf, responseSender) -> {
			readMapUpdate(client, buf, ScatteredShardsAPIImpl.shardSets::putAll, ScatteredShardsAPIImpl.shardData::putAll);
		});
	}

	public static void register() {
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			s2cUpdateShards(Collections.singletonList(handler.player));
		});
	}
}
