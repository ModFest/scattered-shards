package net.modfest.scatteredshards.client.screen.util;

import java.util.List;

import com.mojang.datafixers.util.Either;

import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.WItem;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

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
	
	public static class ShardIcon extends WItem {
		
		protected Either<ItemStack, Identifier> icon = Either.left(ItemStack.EMPTY);
		private final float scale;
		
		public ShardIcon(float scale) {
			super(ItemStack.EMPTY);
			this.scale = scale;
		}
		
		public ShardIcon setIcon(Either<ItemStack, Identifier> icon) {
			this.icon = icon;
			return this;
		}
		
		public ShardIcon setIcon(ItemStack itemStack) {
			this.icon = Either.left(itemStack);
			return this;
		}
		
		public ShardIcon setIcon(Identifier image) {
			this.icon = Either.right(image);
			return this;
		}
		
		@Override
		public void paint(GuiGraphics context, int x, int y, int mouseX, int mouseY) {
			icon.ifLeft(it -> {
				this.setItems(List.of(it));
				WScalableWidgets.paint(context, x, y, scale, () -> super.paint(context, x, y, mouseX, mouseY));
			});
			
			icon.ifRight(it -> {
				ScreenDrawing.texturedRect(context, x, y, getWidth(), getHeight(), it, 0xFF_FFFFFF);
			});
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
