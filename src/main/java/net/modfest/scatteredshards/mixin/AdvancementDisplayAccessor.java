package net.modfest.scatteredshards.mixin;

import com.google.gson.JsonObject;
import net.minecraft.advancement.AdvancementDisplay;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(AdvancementDisplay.class)
public interface AdvancementDisplayAccessor {

	@Invoker("iconFromJson")
	static ItemStack scattered_shards$iconFromJson(JsonObject object) {
		throw new UnsupportedOperationException();
	}
}
