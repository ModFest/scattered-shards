package net.modfest.scatteredshards.client.screen;

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

import com.mojang.datafixers.util.Either;

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
	
	public WShardPanel setType(ShardType value) {
		backing.setImage(value.getFrontTexture());
		typeDescription.setText(value::getDescription);
		return this;
	}
	
	public WShardPanel setIcon(Either<ItemStack, Identifier> icon) {
		this.icon.setIcon(icon);
		return this;
	}
	
	public WShardPanel setName(Supplier<Text> text) {
		this.name.setText(text);
		return this;
	}
	
	public WShardPanel setSource(Supplier<Text> text) {
		this.source.setText(text);
		return this;
	}

	public WShardPanel setShard(Shard shard) {
		this.shard = shard;
		
		setType(shard.shardType());
		setIcon(shard.icon());
		setName(shard::name);
		setSource(shard::source);
		
		return this;
	}
	
	public WShardPanel() {
		add(backing, 0, 40, 48, 64);
		add(icon, 8, 48, 16, 16);
		add(name, 13, 0);
		add(typeDescription, 14, 16);
		add(source, 15, 25);
		add(hint, 15, 120);
	}
	
	public WShardPanel(Shard shard) {
		this();
		setShard(shard);
	}
	
	public Shard getShard() {
		return shard;
	}
}
