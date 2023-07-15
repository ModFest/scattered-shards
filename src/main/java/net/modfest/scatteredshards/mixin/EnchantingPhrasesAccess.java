package net.modfest.scatteredshards.mixin;

import net.minecraft.client.gui.screen.ingame.EnchantingPhrases;
import net.minecraft.text.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(EnchantingPhrases.class)
public interface EnchantingPhrasesAccess {

	@Accessor("STYLE")
	static Style scattered_shards$getStyle() {
		throw new AssertionError();
	}
}
