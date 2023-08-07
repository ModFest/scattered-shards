package net.modfest.scatteredshards.client.screen.util;

import io.github.cottonmc.cotton.gui.widget.TooltipBuilder;
import net.minecraft.text.Text;

import java.util.function.Supplier;

public class WDynamicTooltipLabel extends WShadowLabel {

	private Supplier<Text> tooltip;

	public WDynamicTooltipLabel(Text text, int color, float scale) {
		super(text, color, scale);
	}

	public WDynamicTooltipLabel setTooltip(Supplier<Text> tooltip) {
		this.tooltip = tooltip;
		return this;
	}

	@Override
	public void addTooltip(TooltipBuilder tooltip) {
		if (this.tooltip == null) {
			return;
		}
		tooltip.add(this.tooltip.get());
	}
}
