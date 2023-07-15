package net.modfest.scatteredshards.client.screen;

import com.mojang.datafixers.util.Either;
import io.github.cottonmc.cotton.gui.widget.*;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.core.api.shard.Shard;
import net.modfest.scatteredshards.mixin.EnchantingPhrasesAccess;

import java.util.function.Supplier;

public class WShardPanel extends WPlainPanel {

	private static class WStackIcon extends WItem {

		public WStackIcon(ItemStack stack) {
			super(stack);
		}

		@Override
		public void paint(GuiGraphics context, int x, int y, int mouseX, int mouseY) {
			context.getMatrices().push();
			context.getMatrices().translate(-x, -y, 0.0f);
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

	private WLabel createLabel(Supplier<Text> supplier, int color) {
		var label = dynamic
				? new WDynamicTextLabel(supplier, color)
				: new WLabel(supplier.get(), color);
		return label.setHorizontalAlignment(HorizontalAlignment.CENTER);
	}

	private WLabel createHintLabel(Supplier<Text> hint, int color) {
		return createLabel(() -> {
			return hint.get().copy().fillStyle(EnchantingPhrasesAccess.scattered_shards$getStyle());
		}, color);
	}

	public WShardPanel(Shard shard, boolean dynamic) {
		this.dynamic = dynamic;

		WSprite backing = new WSprite(shard.shardType().getBackingTexture());
		add(backing, 0, 20, 48, 64);

		WWidget icon = createIcon(shard.icon());
		add(icon, 8, 28, 16, 16);

		WLabel name = createLabel(shard::name, 0xFFFFFF);
		name.setSize(20, -1);
		add(name, 14, 0);

		WLabel typeDescription = createLabel(() -> shard.shardType().getDescription(), shard.shardType().textColor());
		add(typeDescription, 14, 100);

		WLabel lore = createLabel(() -> null, null);

		add(createHintLabel(shard::hint, 0xFFFFFF), 14, 120);
	}
}
