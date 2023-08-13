package net.modfest.scatteredshards.client.screen.widget;

import io.github.cottonmc.cotton.gui.widget.WLabel;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.text.Text;

import java.util.function.Supplier;

public class WDynamicLabel extends WDynamicTooltipLabel {

	private Supplier<Text> dynamicText;
	private Supplier<Integer> dynamicColor;

	public WDynamicLabel(Supplier<Text> text, Supplier<Integer> color, float scale) {
		super(null, -1, scale);
		this.dynamicText = text;
		this.dynamicColor = color;
	}

	@Override
	public void paint(GuiGraphics context, int x, int y, int mouseX, int mouseY) {
		text = dynamicText.get();
		color = dynamicColor.get();
		super.paint(context, x, y, mouseX, mouseY);
	}

	@Override
	public WLabel setText(Text text) {
		return setValues(() -> text, () -> this.color);
	}

	public WLabel setValues(Supplier<Text> text, Supplier<Integer> color) {
		dynamicText = text;
		dynamicColor = color;
		return this;
	}
}
