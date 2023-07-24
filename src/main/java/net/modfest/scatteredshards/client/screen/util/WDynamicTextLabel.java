package net.modfest.scatteredshards.client.screen.util;

import io.github.cottonmc.cotton.gui.widget.WLabel;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.text.Text;

import java.util.function.Supplier;

public class WDynamicTextLabel extends WDynamicTooltipLabel {

	private final Supplier<Text> dynamicText;

	public WDynamicTextLabel(Supplier<Text> text, int color, float scale) {
		super(null, color, scale);
		this.dynamicText = text;
	}

	@Override
	public void paint(GuiGraphics context, int x, int y, int mouseX, int mouseY) {
		text = dynamicText.get();
		super.paint(context, x, y, mouseX, mouseY);
	}
}
