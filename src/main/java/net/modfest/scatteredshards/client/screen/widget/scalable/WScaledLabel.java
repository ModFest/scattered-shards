package net.modfest.scatteredshards.client.screen.widget.scalable;

import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import io.github.cottonmc.cotton.gui.client.Scissors;
import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import io.github.cottonmc.cotton.gui.widget.data.VerticalAlignment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.util.math.MathHelper;
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
	public void paint(DrawContext context, int x, int y, int mouseX, int mouseY) {
		//Paint background here because it's one pixel more accurate; results are validated for scaled painting already.
		if (backgroundColor != 0) ScreenDrawing.coloredRect(context, x, y, getWidth(), getHeight(), backgroundColor);
		
		Scissors.push(x, y, width, height);
		super.paint(context, x, y, mouseX, mouseY);
		Scissors.pop();
		
		if (mouseX >= 0 && mouseX < width && mouseY >= 0 && mouseY < height) {
			List<OrderedText> tooltip = hover.get();
			if (!tooltip.isEmpty()) {
				context.drawTooltip(MinecraftClient.getInstance().textRenderer, tooltip, HoveredTooltipPositioner.INSTANCE, x + mouseX, y + mouseY);
			}
		}
	}
	
	@Override
	public void paintScaled(DrawContext context, int width, int height, int mouseX, int mouseY) {
		MinecraftClient mc = MinecraftClient.getInstance();
		TextRenderer renderer = mc.textRenderer;
		int yOffset = switch (verticalAlignment) {
			case CENTER -> height / 2 - renderer.fontHeight / 2;
			case BOTTOM -> height - renderer.fontHeight;
			case TOP -> 0;
		};
		
		boolean hovered = (mouseX>=0 && mouseY>=0 && mouseX<getWidth() && mouseY<getHeight());
		drawScrollableString(context, text.get().asOrderedText(), horizontalAlignment, 0, yOffset, width, color.getAsInt(), shadow, hovered);
		
	}
	
	public static void drawScrollableString(DrawContext context, OrderedText text, HorizontalAlignment alignment, int x, int y, int width, int color, boolean shadow, boolean scroll) {
		MinecraftClient mc = MinecraftClient.getInstance();
		TextRenderer font = mc.textRenderer;
		int textWidth = font.getWidth(text);
		int xofs = 0;
		
		if (textWidth > width && scroll) {
			alignment = HorizontalAlignment.LEFT;
			int scrollWidth = textWidth - width;
			double seconds = Util.getMeasuringTimeMs() / 1000.0;
			double scrollSpeed = Math.max(scrollWidth * 0.5, 3.0);
			double t = Math.sin((Math.PI / 2) * Math.cos((Math.PI * 2) * seconds / scrollSpeed)) / 2.0 + 0.5;
			xofs = (int) MathHelper.lerp(t, 0.0, scrollWidth);
		}
		
		context.setShaderColor(1, 1, 1, 1);
		if (shadow) {
			ScreenDrawing.drawStringWithShadow(context, text, alignment, x - (int) xofs, y, width, color);
		} else {
			ScreenDrawing.drawString(context, text, alignment, x - (int) xofs, y, width, color);
		}
	}
	
	
	@Override
	public boolean canResize() {
		return true;
	}

}
