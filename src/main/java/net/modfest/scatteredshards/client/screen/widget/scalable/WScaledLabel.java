package net.modfest.scatteredshards.client.screen.widget.scalable;

import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import io.github.cottonmc.cotton.gui.widget.data.VerticalAlignment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.tooltip.DefaultTooltipPositioner;
import net.minecraft.text.Text;
import net.minecraft.text.OrderedText;

public class WScaledLabel extends WScalableWidget {
	protected Supplier<Text> text;
	protected IntSupplier color = () -> 0xFF_FFFFFF;
	protected Supplier<List<OrderedText>> hover = () -> List.of();
	protected boolean shadow = false;
	protected int backgroundColor = 0;
	
	protected VerticalAlignment verticalAlignment = VerticalAlignment.TOP;
	protected HorizontalAlignment horizontalAlignment = HorizontalAlignment.LEFT;
	
	public WScaledLabel(Text text, float scale) {
		this.text = () -> text;
		this.scale = scale;
	}
	
	public WScaledLabel(Supplier<Text> text, float scale) {
		this.text = text;
		this.scale = scale;
	}
	
	public WScaledLabel setText(Text text) {
		this.text = () -> text;
		return this;
	}
	
	public WScaledLabel setText(Supplier<Text> text) {
		this.text = text;
		return this;
	}
	
	public WScaledLabel setColor(int color) {
		this.color = () -> color;
		return this;
	}
	
	public WScaledLabel setColor(IntSupplier color) {
		this.color = color;
		return this;
	}
	
	public WScaledLabel setHover(Supplier<Text> text) {
		this.hover = () -> MinecraftClient.getInstance().textRenderer.wrapLines(text.get(), 200);
		return this;
	}
	
	public WScaledLabel setHoverLines(Supplier<List<OrderedText>> hover) {
		this.hover = hover;
		return this;
	}
	
	public WScaledLabel setHorizontalAlignment(HorizontalAlignment value) {
		this.horizontalAlignment = value;
		return this;
	}
	
	public WScaledLabel setVerticalAlignment(VerticalAlignment value) {
		this.verticalAlignment = value;
		return this;
	}
	
	public WScaledLabel setShadow(boolean value) {
		this.shadow = value;
		return this;
	}
	
	@SuppressWarnings("resource")
	@Override
	public void paint(GuiGraphics context, int x, int y, int mouseX, int mouseY) {
		//Paint background here because it's one pixel more accurate; results are validated for scaled painting already.
		if (backgroundColor != 0) ScreenDrawing.coloredRect(context, x, y, getWidth(), getHeight(), backgroundColor);
		super.paint(context, x, y, mouseX, mouseY);
		
		if (mouseX >= 0 && mouseX < width && mouseY >= 0 && mouseY < height) {
			List<OrderedText> tooltip = hover.get();
			if (!tooltip.isEmpty()) {
				context.drawTooltip(MinecraftClient.getInstance().textRenderer, tooltip, DefaultTooltipPositioner.INSTANCE, x + mouseX, y + mouseY);
			}
		}
	}
	
	@Override
	public void paintScaled(GuiGraphics context, int width, int height, int mouseX, int mouseY) {
		MinecraftClient mc = MinecraftClient.getInstance();
		TextRenderer renderer = mc.textRenderer;
		int yOffset = switch (verticalAlignment) {
			case CENTER -> height / 2 - renderer.fontHeight / 2;
			case BOTTOM -> height - renderer.fontHeight;
			case TOP -> 0;
		};
		
		if (shadow) {
			ScreenDrawing.drawStringWithShadow(context, text.get().asOrderedText(), horizontalAlignment, 0, yOffset, width, color.getAsInt());
		} else {
			ScreenDrawing.drawString(context, text.get().asOrderedText(), horizontalAlignment, 0, yOffset, width, color.getAsInt());
		}
	}
	
	@Override
	public boolean canResize() {
		return true;
	}

}
