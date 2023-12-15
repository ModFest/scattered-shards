package net.modfest.scatteredshards.client.screen.widget.scalable;

import java.util.List;
import java.util.function.IntSupplier;
import java.util.function.Supplier;

import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import io.github.cottonmc.cotton.gui.widget.data.VerticalAlignment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.tooltip.HoveredTooltipPositioner;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;

/**
 * A scaled multiline label widget. See {@link io.github.cottonmc.cotton.gui.widget.WText WText}.
 */
public class WScaledText extends WScalableWidget {
	
	protected Supplier<Text> text;
	protected IntSupplier color = () -> 0xFF_FFFFFF;
	protected Supplier<List<OrderedText>> hover = () -> List.of();
	protected boolean shadow = false;
	protected int backgroundColor = 0;
	
	protected VerticalAlignment verticalAlignment = VerticalAlignment.TOP;
	protected HorizontalAlignment horizontalAlignment = HorizontalAlignment.LEFT;
	
	public WScaledText(Text text, float scale) {
		this.text = () -> text;
		this.scale = scale;
	}
	
	public WScaledText(Supplier<Text> text, float scale) {
		this.text = text;
		this.scale = scale;
	}
	
	public WScaledText setText(Text text) {
		this.text = () -> text;
		return this;
	}
	
	public WScaledText setText(Supplier<Text> text) {
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
	
	public WScaledText setHover(Supplier<Text> text) {
		this.hover = () -> MinecraftClient.getInstance().textRenderer.wrapLines(text.get(), 200);
		return this;
	}
	
	public WScaledText setHoverLines(Supplier<List<OrderedText>> hover) {
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
	
	@SuppressWarnings("resource")
	@Override
	public void paint(DrawContext context, int x, int y, int mouseX, int mouseY) {
		//Paint background here because it's one pixel more accurate; results are validated for scaled painting already.
		if (backgroundColor != 0) ScreenDrawing.coloredRect(context, x, y, getWidth(), getHeight(), backgroundColor);
		super.paint(context, x, y, mouseX, mouseY);
		
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
		int frameColor = color.getAsInt();
		List<OrderedText> lines = renderer.wrapLines(text.get(), width);
		
		int totalHeight = renderer.fontHeight * lines.size();
		
		int yOffset = switch (verticalAlignment) {
			case CENTER -> height / 2 - totalHeight / 2;
			case BOTTOM -> height - totalHeight;
			case TOP -> 0;
		};
		
		for(int i=0; i<lines.size(); i++) {
			int lineY = renderer.fontHeight * i;
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
