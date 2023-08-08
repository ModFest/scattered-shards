package net.modfest.scatteredshards.client.screen;

import com.mojang.datafixers.util.Either;
import io.github.cottonmc.cotton.gui.widget.WPlainPanel;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.api.shard.ShardType;
import net.modfest.scatteredshards.client.screen.util.WDynamicLabel;
import net.modfest.scatteredshards.client.screen.util.WDynamicSprite;
import net.modfest.scatteredshards.client.screen.util.WScalableWidgets;

import java.util.function.Supplier;

public class WShardPanel extends WPlainPanel {

	private static final Identifier TEXTURE = ScatteredShards.id("textures/gui/view.png");

	public static final Supplier<Integer> WHITE = () -> 0xFFFFFF;
	public static final Style HINT_STYLE = Style.EMPTY.withFont(new Identifier("minecraft:alt"));

	private Shard shard;

	private final WDynamicSprite backing = new WDynamicSprite(ShardType.MISSING.getFrontTexture());
	private final WScalableWidgets.ShardIcon icon = new WScalableWidgets.ShardIcon(2.0f);
	private final WDynamicLabel name = createLabel(Shard.MISSING_SHARD::name, WHITE, 1.14f);
	private final WDynamicLabel typeDescription = createLabel(ShardType.MISSING::getDescription, ShardType.MISSING::textColor, 0.9f);
	private final WDynamicLabel source = createLabel(Shard.MISSING_SHARD::source, WHITE, 0.9f);
	private final WDynamicLabel lore = createLabel(Shard.MISSING_SHARD::lore, WHITE, 0.8f);
	private final WDynamicLabel hint = createHintLabel(Shard.MISSING_SHARD::hint, WHITE, 0.8f);

	private static WDynamicLabel createLabel(Supplier<Text> supplier, Supplier<Integer> color, float scale) {
		var label = new WDynamicLabel(supplier, color, scale);
		label.setHorizontalAlignment(HorizontalAlignment.CENTER);
		label.setShadow(true);
		return label;
	}

	private WDynamicLabel createHintLabel(Supplier<Text> hint, Supplier<Integer> color, float scale) {
		var label = createLabel(() -> {
			return hint.get().copy().fillStyle(HINT_STYLE);
		}, color, scale);
		label.setTooltip(hint);
		return label;
	}

	/**
	 * Sets the shardType displayed to a static value. Note: Prevents the shardType from being updated if the configured shard is mutated!
	 */
	public WShardPanel setType(ShardType value) {
		backing.setImage(value::getFrontTexture);
		typeDescription.setValues(value::getDescription, value::textColor);
		return this;
	}

	/**
	 * Sets the icon displayed to a static value. Note: Prevents shard icon from being updated if the configured shard is mutated!
	 */
	public WShardPanel setIcon(Either<ItemStack, Identifier> icon) {
		this.icon.setIcon(icon);
		return this;
	}

	public WShardPanel setName(Supplier<Text> text, Supplier<Integer> color) {
		this.name.setValues(text, color);
		return this;
	}

	public WShardPanel setSource(Supplier<Text> text, Supplier<Integer> color) {
		this.source.setValues(text, color);
		return this;
	}

	public WShardPanel setLore(Supplier<Text> text, Supplier<Integer> color) {
		this.lore.setValues(() -> text.get().copy().formatted(Formatting.ITALIC), color);
		return this;
	}

	public WShardPanel setHint(Supplier<Text> text, Supplier<Integer> color) {
		this.hint.setValues(() -> text.get().copy().fillStyle(HINT_STYLE), color);
		this.hint.setTooltip(text);
		return this;
	}

	public WShardPanel setShard(Shard shard) {
		this.shard = shard;

		backing.setImage(() -> shard.shardType().getFrontTexture());
		typeDescription.setValues(() -> shard.shardType().getDescription(), () -> shard.shardType().textColor());
		icon.setIcon(shard::icon);
		setName(shard::name, WHITE);
		setSource(shard::source, WHITE);
		setLore(shard::lore, WHITE);
		setHint(shard::hint, WHITE);

		return this;
	}

	public WShardPanel() {
		this.setSize(48, 128);

		setBackgroundPainter((context, left, top, panel) -> {
			context.drawTexture(TEXTURE, left, top, 0, 0, 114, 200);
		});

		int xo = 34;
		int yo = 27;

		add(backing, xo, 40 + yo, 48, 64);
		add(icon, 8 + xo, 48 + yo, 16, 16);
		add(name, 13 + xo, yo);
		add(typeDescription, 14 + xo, 16 + yo);
		add(source, 15 + xo, 25 + yo);
		add(lore, 16 + xo, 113 + yo);
		add(hint, 16 + xo, 135 + yo);
	}

	public WShardPanel(Shard shard) {
		this();
		setShard(shard);
	}

	public Shard getShard() {
		return shard;
	}
}
