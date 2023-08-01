package net.modfest.scatteredshards.client.screen;

import com.mojang.datafixers.util.Either;
import io.github.cottonmc.cotton.gui.widget.*;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import net.minecraft.client.util.ColorUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.client.screen.util.WDynamicTextLabel;
import net.modfest.scatteredshards.client.screen.util.WDynamicTooltipLabel;
import net.modfest.scatteredshards.client.screen.util.WScalableWidgets;

import java.util.function.Supplier;

public class WShardPanel extends WPlainPanel {

	public static final Style HINT_STYLE = Style.EMPTY.withFont(new Identifier("minecraft:alt"));

	private final boolean dynamic;

	private static WWidget createIcon(Either<ItemStack, Identifier> icon) {
		if (icon.left().isPresent()) {
			return new WScalableWidgets.Item(icon.left().get(), 2.0f);
		} else if (icon.right().isPresent()) {
			return new WSprite(icon.right().get());
		} else {
			throw new UnsupportedOperationException();
		}
	}

	private WDynamicTooltipLabel createLabel(Supplier<Text> supplier, int color, float scale) {
		var label = dynamic
				? new WDynamicTextLabel(supplier, color, scale)
				: new WDynamicTooltipLabel(supplier.get(), color, scale);
		label.setHorizontalAlignment(HorizontalAlignment.CENTER);
		label.setShadow(true);
		return label;
	}

	private WLabel createHintLabel(Supplier<Text> hint, int color, float scale) {
		var label = createLabel(() -> {
			return hint.get().copy().fillStyle(HINT_STYLE);
		}, color, scale);
		return label.setTooltip(hint);
	}

	private void addLabel(Supplier<WLabel> label, int x, int y) {
		//Color.RGB color = (Color.RGB) Color.rgb(label.get().getColor());
		//Color.HSL darker = new Color.HSL(color.getHue(), color.getHSLSaturation(), color.getLuma() / 3.0f);

		int darker = ColorUtil.ARGB32.lerp(0.5f, label.get().getColor(), 0);
		var shadow = label.get().setColor(darker);
		add(shadow, x + 1, y + 1);
		add(label.get(), x, y);
	}

	public WShardPanel(Shard shard, boolean dynamic) {
		this.dynamic = dynamic;

		WSprite backing = new WSprite(shard.shardType().getFrontTexture());
		add(backing, 0, 40, 48, 64);

		WWidget icon = createIcon(shard.icon());
		add(icon, 8, 48, 16, 16);

		WLabel name = createLabel(shard::name, 0xFFFFFF, 1.14f);
		add(name, 13, 0);

		WLabel typeDescription = createLabel(() -> shard.shardType().getDescription(), shard.shardType().textColor(), 0.9f);
		add(typeDescription, 14, 16);

		WLabel source = createLabel(shard::source, 0xFFFFFF, 0.9f);
		add(source, 15, 25);

		add(createHintLabel(shard::hint, 0xFFFFFF, 0.8f), 15, 120);
	}
}
