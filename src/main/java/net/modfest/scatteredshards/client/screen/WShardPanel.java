package net.modfest.scatteredshards.client.screen;

import com.mojang.datafixers.util.Either;
import io.github.cottonmc.cotton.gui.widget.*;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.core.api.shard.Shard;

import java.util.function.Supplier;

public class WShardPanel extends WPlainPanel {

	private static class WStackIcon extends WItem {

		public WStackIcon(ItemStack stack) {
			super(stack);
		}

		@Override
		public void paint(GuiGraphics context, int x, int y, int mouseX, int mouseY) {
			context.getMatrices().push();
			context.getMatrices().translate(x * -0.5f, y * -0.5f, 0.0f);
			context.getMatrices().scale(2.0f, 2.0f, 1.0f);
			super.paint(context, x, y, mouseX, mouseY);
			context.getMatrices().pop();
		}
	}

	private static class WDynamicTextLabel extends WLabel {

		private final Supplier<Text> dynamicText;

		public WDynamicTextLabel(Supplier<Text> text, int color) {
			super(null, color);
			this.dynamicText = text;
		}

		@Override
		public void paint(GuiGraphics context, int x, int y, int mouseX, int mouseY) {
			text = dynamicText.get();
			super.paint(context, x, y, mouseX, mouseY);
		}
	}

	private final boolean dynamic;

	private static WWidget createIcon(Either<ItemStack, Identifier> icon) {
		if (icon.left().isPresent()) {
			return new WStackIcon(icon.left().get());
		} else if (icon.right().isPresent()) {
			return new WSprite(icon.right().get());
		} else {
			throw new UnsupportedOperationException();
		}
	}

	private WWidget createLabel(Supplier<Text> supplier, int color) {
		if (dynamic) {
			return new WDynamicTextLabel(supplier, color);
		} else {
			return new WLabel(supplier.get(), color);
		}
	}

	public WShardPanel(Shard shard, boolean dynamic) {
		this.dynamic = dynamic;

		WSprite backing = new WSprite(shard.backingTexture());
		add(backing, 0, 20, 48, 64);

		WWidget icon = createIcon(shard.icon());
		add(icon, 8, 8, 32, 32);
	}
}
