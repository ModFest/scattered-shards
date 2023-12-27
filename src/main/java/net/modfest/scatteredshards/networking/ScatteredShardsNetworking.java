package net.modfest.scatteredshards.networking;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.modfest.scatteredshards.api.impl.ShardCollectionPersistentState;
import net.modfest.scatteredshards.api.impl.ShardLibraryPersistentState;

public class ScatteredShardsNetworking {
	
	@Environment(EnvType.CLIENT)
	public static void registerClient() {
		ClientPlayNetworking.registerGlobalReceiver(S2CSyncShard.ID, S2CSyncShard::receive);
		ClientPlayNetworking.registerGlobalReceiver(S2CDeleteShard.ID, S2CDeleteShard::receive);
		ClientPlayNetworking.registerGlobalReceiver(S2CSyncLibrary.ID, S2CSyncLibrary::receive);
		ClientPlayNetworking.registerGlobalReceiver(S2CSyncCollection.ID, S2CSyncCollection::receive);
		
		ClientPlayNetworking.registerGlobalReceiver(S2CCollectShard.ID, S2CCollectShard::receive);
		ClientPlayNetworking.registerGlobalReceiver(S2CUncollectShard.ID, S2CUncollectShard::receive);
		ClientPlayNetworking.registerGlobalReceiver(S2CModifyShardResult.ID, S2CModifyShardResult::receive);
	}

	public static void register() {
		ServerPlayNetworking.registerGlobalReceiver(C2SModifyShard.ID, C2SModifyShard::receive);
		
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			ShardLibraryPersistentState.get(server); // Trigger the PersistentState load if it hasn't yet
			S2CSyncLibrary.send(handler.getPlayer());
			ShardCollectionPersistentState.get(server); // Trigger the PersistentState load if it hasn't yet
			S2CSyncCollection.send(handler.getPlayer());
		});
		
	}
}
