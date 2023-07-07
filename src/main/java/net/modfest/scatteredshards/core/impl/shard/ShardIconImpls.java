package net.modfest.scatteredshards.core.impl.shard;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class ShardIconImpls {

	public record TextureBacked(Identifier texture) {

		public void render(GuiGraphics graphics, int x, int y) {
			graphics.drawTexture(texture, x, y, 0, 0, 16, 16);
		}
	}

	public record StackBacked(ItemStack stack) {

		public void render(GuiGraphics graphics, int x, int y) {
			graphics.drawItemWithoutEntity(stack, x, y);
		}
	}
}
