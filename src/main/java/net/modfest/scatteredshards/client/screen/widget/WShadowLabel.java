package net.modfest.scatteredshards.client.screen.widget;

import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.quiltmc.loader.api.minecraft.ClientOnly;

import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Style;
import net.minecraft.text.Text;

public class WShadowLabel extends WScalableWidgets.Label {

	private boolean shadow;
	protected boolean multiline = true;

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
		
		List<OrderedText> lines = renderer.wrapLines(text, (int) (this.getWidth() / scale));
		int renderedHeight = (multiline) ? lines.size() * renderer.fontHeight : renderer.fontHeight;
		
		int yOffset = switch (verticalAlignment) {
			case CENTER -> height / 2 - renderedHeight / 2;
			case BOTTOM -> height - renderedHeight;
			case TOP -> 0;
		};

		int useColor = shouldRenderInDarkMode() ? darkmodeColor : color;

		WScalableWidgets.paint(context, x, y, scale, () -> {
			int yi = y + yOffset;
			int numLines = (multiline) ? lines.size() : 1;
			for(int i=0; i<numLines; i++) {
				if (shadow) {
					ScreenDrawing.drawStringWithShadow(context, lines.get(i), horizontalAlignment, x, yi, this.getWidth(), useColor);
				} else {
					ScreenDrawing.drawString(context, lines.get(i), horizontalAlignment, x, yi, this.getWidth(), useColor);
				}
				yi += renderer.fontHeight;
			}
		});
		
		//TODO: This is better than it was before, but it's still a massive oversimplification and won't produce correct hover rects
		Style hoveredTextStyle = getTextStyleAt((int) (mouseX / scale), (int) (mouseY / scale));
		ScreenDrawing.drawTextHover(context, hoveredTextStyle, x + mouseX, y + mouseY);
	}
	
	@ClientOnly
	@Nullable
	@Override
	public Style getTextStyleAt(int x, int y) {
		@SuppressWarnings("resource")
		TextRenderer renderer = MinecraftClient.getInstance().textRenderer;
		List<OrderedText> lines = renderer.wrapLines(text, (int) (this.getWidth() / scale));
		int renderedHeight = (multiline) ? lines.size() * renderer.fontHeight : renderer.fontHeight;
		
		int yOffset = switch (verticalAlignment) {
			case CENTER -> height / 2 - renderedHeight / 2;
			case BOTTOM -> height - renderedHeight;
			case TOP -> 0;
		};
		
		int lineIndex = y / renderer.fontHeight - yOffset;

		if (lineIndex >= 0 && lineIndex < lines.size()) {
			OrderedText line = lines.get(lineIndex);
			return renderer.getTextHandler().getStyleAt(line, x);
		}

		return null;
	}
}
