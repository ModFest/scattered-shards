package net.modfest.scatteredshards.client.screen.util;

import io.github.cottonmc.cotton.gui.widget.WItem;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;

public class WScalableWidgets {

	public static void paint(GuiGraphics context, int x, int y, float scale, Runnable runnable) {
		context.getMatrices().push();
		context.getMatrices().translate(x - x * scale, y - y * scale, 0.0f);
		context.getMatrices().scale(scale, scale, 1.0f);
		runnable.run();
		context.getMatrices().pop();
	}

	public static class Item extends WItem {

		private final float scale;

		public Item(ItemStack stack, float scale) {
			super(stack);
			this.scale = scale;
		}

		@Override
		public void paint(GuiGraphics context, int x, int y, int mouseX, int mouseY) {
			WScalableWidgets.paint(context, x, y, scale, () -> super.paint(context, x, y, mouseX, mouseY));
		}
	}

	public static abstract class Label extends WLabel {

		protected final float scale;

		public Label(Text text, int color, float scale) {
			super(text, color);
			this.scale = scale;
		}
	}
}
