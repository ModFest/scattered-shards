package net.modfest.scatteredshards.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.layouts.GridLayout;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.network.chat.Component;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.client.ScatteredShardsClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(PauseScreen.class)
public class PauseScreenMixin {
	@WrapOperation(method = "createPauseMenu", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/layouts/GridLayout$RowHelper;addChild(Lnet/minecraft/client/gui/layouts/LayoutElement;)Lnet/minecraft/client/gui/layouts/LayoutElement;", ordinal = 0))
	private LayoutElement replaceAdvancements(GridLayout.RowHelper instance, LayoutElement widget, Operation<LayoutElement> original) {
		if (!ScatteredShards.CONFIG.replace_advancements.value()) return original.call(instance, widget);
		return instance.addChild(Button.builder(Component.translatable("menu.scattered_shards.collection"), b -> ScatteredShardsClient.openShardTablet()).width(98).build());
	}
}
