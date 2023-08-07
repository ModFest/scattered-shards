package net.modfest.scatteredshards.client.screen;

import com.mojang.datafixers.util.Either;

import io.github.cottonmc.cotton.gui.widget.WWidget;
import io.github.cottonmc.cotton.gui.widget.WPlainPanel;
import io.github.cottonmc.cotton.gui.widget.WSprite;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.api.shard.ShardType;
import net.modfest.scatteredshards.client.screen.util.WDynamicTextLabel;
import net.modfest.scatteredshards.client.screen.util.WScalableWidgets;

import java.util.function.Supplier;

public class WShardPanel extends WPlainPanel {

	public static final Style HINT_STYLE = Style.EMPTY.withFont(new Identifier("minecraft:alt"));

	private Shard shard;
	
	private final WSprite backing = new WSprite(ShardType.MISSING.getFrontTexture());
	private final WScalableWidgets.ShardIcon icon = new WScalableWidgets.ShardIcon(2.0f);
	private final WDynamicTextLabel name = createLabel(Shard.MISSING_SHARD::name, 0xFFFFFF, 1.14f);
	private final WDynamicTextLabel typeDescription = createLabel(ShardType.MISSING::getDescription, ShardType.MISSING.textColor(), 0.9f);
	private final WDynamicTextLabel source = createLabel(Shard.MISSING_SHARD::source, 0xFFFFFF, 0.9f);
	private final WDynamicTextLabel hint = createHintLabel(Shard.MISSING_SHARD::hint, 0xFFFFFF, 0.8f);

	private static WDynamicTextLabel createLabel(Supplier<Text> supplier, int color, float scale) {
		var label = new WDynamicTextLabel(supplier, color, scale);
		label.setHorizontalAlignment(HorizontalAlignment.CENTER);
		label.setShadow(true);
		return label;
	}

	private WDynamicTextLabel createHintLabel(Supplier<Text> hint, int color, float scale) {
		var label = createLabel(() -> {
			return hint.get().copy().fillStyle(HINT_STYLE);
		}, color, scale);
		label.setTooltip(hint);
		return label;
	}

	public WShardPanel setShard(Shard shard) {
		this.shard = shard;
		
		backing.setImage(shard.shardType().getFrontTexture());
		icon.setIcon(shard.icon());
		name.setText(shard.name());
		typeDescription.setText(shard.shardType()::getDescription);
		source.setText(shard::source);
		
		return this;
	}
	
	public WShardPanel(Shard shard, boolean dynamic) {
		add(backing, 0, 40, 48, 64);
		add(icon, 8, 48, 16, 16);
		add(name, 13, 0);
		add(typeDescription, 14, 16);
		add(source, 15, 25);
		add(hint, 15, 120);
		
		setShard(shard);
	}
}
