package net.modfest.scatteredshards.client.screen.widget;

import java.util.function.Supplier;

import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.WWidget;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;

public class WDynamicSprite extends WWidget {
	protected Supplier<Identifier> image = () -> new Identifier("");
	protected int tint = 0xFF_FFFFFF;
	
	public WDynamicSprite() {
	}
	
	public WDynamicSprite(Identifier image) {
		setImage(image);
	}

	public WDynamicSprite(Supplier<Identifier> image) {
		setImage(image);
	}
	
	public void setImage(Identifier image) {
		this.image = () -> image;
	}
	
	public void setImage(Supplier<Identifier> image) {
		this.image = image;
	}
	
	public void setTint(int color) {
		this.tint = color;
	}
	
	@Override
	public boolean canResize() {
		return true;
	}
	
	@Override
	public void paint(DrawContext context, int x, int y, int mouseX, int mouseY) {
		ScreenDrawing.texturedRect(context, x, y, getWidth(), getHeight(), image.get(), tint);
	}
}
