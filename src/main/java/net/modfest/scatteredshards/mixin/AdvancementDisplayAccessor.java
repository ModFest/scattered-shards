package net.modfest.scatteredshards.mixin;

import com.google.gson.JsonObject;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AdvancementDisplay.class)
public class AdvancementDisplayAccessor {

	@Invoker("iconFromJson")
	public static ItemStack iconFromJson(JsonObject object) {
		throw new UnsupportedOperationException();
	}
}
