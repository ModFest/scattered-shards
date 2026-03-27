package net.modfest.scatteredshards.client.screen.widget.scalable;

import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import io.github.cottonmc.cotton.gui.widget.data.VerticalAlignment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.tooltip.DefaultTooltipPositioner;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.Component;

import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

/**
 * A scaled multiline label widget. See {@link io.github.cottonmc.cotton.gui.widget.WText WText}.
 */
public class WScaledText extends WScalableWidget {

	protected Supplier<Component> text;
	protected IntSupplier color = () -> 0xFF_FFFFFF;
	protected Supplier<List<FormattedCharSequence>> hover = List::of;
	protected boolean shadow = false;
	protected int backgroundColor = 0;

	protected VerticalAlignment verticalAlignment = VerticalAlignment.TOP;
	protected HorizontalAlignment horizontalAlignment = HorizontalAlignment.LEFT;

	public WScaledText(Component text, float scale) {
		this.text = () -> text;
		this.scale = scale;
	}

	public WScaledText(Supplier<Component> text, float scale) {
		this.text = text;
		this.scale = scale;
	}

	public WScaledText setText(Component text) {
		this.text = () -> text;
		return this;
	}

	public WScaledText setText(Supplier<Component> text) {
		this.text = text;
		return this;
	}

	public WScaledText setColor(int color) {
		this.color = () -> color;
		return this;
	}

	public WScaledText setColor(IntSupplier color) {
		this.color = color;
		return this;
	}

	public WScaledText setHover(Supplier<Component> text) {
		this.hover = () -> Minecraft.getInstance().font.split(text.get(), 200);
		return this;
	}

	public WScaledText setHoverLines(Supplier<List<FormattedCharSequence>> hover) {
		this.hover = hover;
		return this;
	}

	public WScaledText setHorizontalAlignment(HorizontalAlignment value) {
		this.horizontalAlignment = value;
		return this;
	}

	public WScaledText setVerticalAlignment(VerticalAlignment value) {
		this.verticalAlignment = value;
		return this;
	}

	public WScaledText setShadow(boolean value) {
		this.shadow = value;
		return this;
	}

	@Override
	public void paint(GuiGraphics context, int x, int y, int mouseX, int mouseY) {
		//Paint background here because it's one pixel more accurate; results are validated for scaled painting already.
		if (backgroundColor != 0) ScreenDrawing.coloredRect(context, x, y, getWidth(), getHeight(), backgroundColor);
		super.paint(context, x, y, mouseX, mouseY);

		if (mouseX >= 0 && mouseX < width && mouseY >= 0 && mouseY < height) {
			List<FormattedCharSequence> tooltip = hover.get();
			if (!tooltip.isEmpty()) {
				context.setTooltipForNextFrame(tooltip, x + mouseX, y + mouseY);
			}
		}
	}

	@Override
	public void paintScaled(GuiGraphics context, int width, int height, int mouseX, int mouseY) {
		Font textRenderer = Minecraft.getInstance().font;
		int frameColor = color.getAsInt();
		List<FormattedCharSequence> lines = textRenderer.split(text.get(), width);

		int totalHeight = textRenderer.lineHeight * lines.size();

		int yOffset = switch (verticalAlignment) {
			case CENTER -> height / 2 - totalHeight / 2;
			case BOTTOM -> height - totalHeight;
			case TOP -> 0;
		};

		for (int i = 0; i < lines.size(); i++) {
			int lineY = textRenderer.lineHeight * i;
			if (shadow) {
				ScreenDrawing.drawStringWithShadow(context, lines.get(i), horizontalAlignment, 0, yOffset + lineY, width, frameColor);
			} else {
				ScreenDrawing.drawString(context, lines.get(i), horizontalAlignment, 0, yOffset + lineY, width, frameColor);
			}
		}
	}

	@Override
	public boolean canResize() {
		return true;
	}
}
