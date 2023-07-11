package net.modfest.scatteredshards.client;

import net.modfest.scatteredshards.networking.ScatteredShardsNetworking;
import org.quiltmc.loader.api.ModContainer;
import org.quiltmc.qsl.base.api.entrypoint.client.ClientModInitializer;

public class ScatteredShardsClient implements ClientModInitializer {

	@Override
	public void onInitializeClient(ModContainer mod) {
		ScatteredShardsNetworking.registerClient();
	}
}
