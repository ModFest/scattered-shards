package net.modfest.scatteredshards.client;

import com.mojang.blaze3d.platform.InputUtil;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBind;
import net.modfest.scatteredshards.client.screen.ShardTabletGuiDescription;
import net.modfest.scatteredshards.component.ScatteredShardsComponents;
import org.lwjgl.glfw.GLFW;
import org.quiltmc.loader.api.minecraft.ClientOnly;
import org.quiltmc.qsl.lifecycle.api.client.event.ClientTickEvents;

public class ScatteredShardsKeybindings {
	public static KeyBind shardsKeyBind = KeyBindingHelper.registerKeyBinding(new KeyBind(
			"key.scattered_shards.shards",
			InputUtil.Type.KEYSYM,
			GLFW.GLFW_KEY_J,
			"category.scattered_shards.scattered_shards"
	));

	@ClientOnly
	public static void registerClient() {
		ClientTickEvents.END.register(client -> {
			while (shardsKeyBind.wasPressed()) {
				if (client.player != null && client.world != null) {
					var collection = ScatteredShardsComponents.getShardCollection(client.player);
					var library = ScatteredShardsComponents.getShardLibrary(client.world);
					client.execute(() -> client.setScreen(new ShardTabletGuiDescription.Screen(collection, library)));
				}
			}
		});
	}
}
