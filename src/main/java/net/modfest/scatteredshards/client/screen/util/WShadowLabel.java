package net.modfest.scatteredshards.client.screen.util;

import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

public class WShadowLabel extends WScalableWidgets.Label {

	private boolean shadow;

	public WShadowLabel(Text text, int color, float scale) {
		super(text, color, scale);
	}

	public WShadowLabel setShadow(boolean shadow) {
		this.shadow = shadow;
		return this;
	}

	@Override
	public void paint(GuiGraphics context, int x, int y, int mouseX, int mouseY) {
		MinecraftClient mc = MinecraftClient.getInstance();
		TextRenderer renderer = mc.textRenderer;

		int yOffset = switch (verticalAlignment) {
			case CENTER -> height / 2 - renderer.fontHeight / 2;
			case BOTTOM -> height - renderer.fontHeight;
			case TOP -> 0;
		};

		int useColor = shouldRenderInDarkMode() ? darkmodeColor : color;

		WScalableWidgets.paint(context, x, y, scale, () -> {
			if (shadow) {
				ScreenDrawing.drawStringWithShadow(context, text.asOrderedText(), horizontalAlignment, x, y + yOffset, this.getWidth(), useColor);
			} else {
				ScreenDrawing.drawString(context, text.asOrderedText(), horizontalAlignment, x, y + yOffset, this.getWidth(), useColor);
			}
		});

		Style hoveredTextStyle = getTextStyleAt(mouseX, mouseY);
		ScreenDrawing.drawTextHover(context, hoveredTextStyle, x + mouseX, y + mouseY);
	}
}
