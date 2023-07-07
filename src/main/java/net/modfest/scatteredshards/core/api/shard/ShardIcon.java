package net.modfest.scatteredshards.core.api.shard;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.modfest.scatteredshards.core.impl.shard.ShardIconImpls;
import net.modfest.scatteredshards.mixin.AdvancementDisplayAccessor;
import org.quiltmc.loader.api.minecraft.ClientOnly;

public interface ShardIcon {

	@ClientOnly
	void render(GuiGraphics graphics, int x, int y);

	static ShardIcon fromJson(JsonObject obj) {
		if (JsonHelper.hasString(obj, "texture")) {
			Identifier texture = new Identifier(JsonHelper.getString(obj, "texture"));
			return new ShardIconImpls.TextureBacked(texture);
		}
		else if (JsonHelper.hasJsonObject(obj, "stack")) {
			JsonObject stackObj = JsonHelper.getObject(obj, "stack");
			ItemStack stack = AdvancementDisplayAccessor.scattered_shards$iconFromJson(stackObj);
			return new ShardIconImpls.StackBacked(stack);
		}
		throw new JsonSyntaxException("shard icon must be either a 'texture' path or a 'stack' object");
	}
}
