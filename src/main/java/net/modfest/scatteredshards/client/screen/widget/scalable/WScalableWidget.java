package net.modfest.scatteredshards.client.screen.widget.scalable;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import io.github.cottonmc.cotton.gui.widget.WWidget;
import net.minecraft.client.gui.DrawContext;

public abstract class WScalableWidget extends WWidget {
	
	protected float scale = 1.0f;
	
	@Environment(EnvType.CLIENT)
	@Override
	public void paint(DrawContext context, int x, int y, int mouseX, int mouseY) {
		context.getMatrices().push();
		context.getMatrices().translate(x, y, 0);
		context.getMatrices().scale(scale, scale, 1.0f);
		
		paintScaled(context, (int) (this.getWidth() / scale), (int) (this.getHeight() / scale), (int) (mouseX / scale), (int) (mouseY / scale));
		context.getMatrices().pop();
	}
	
	/**
	 * Paint the widget from within a scaled context. 0,0 is the top-left corner of the widget.
	 * @param context The graphics context to paint with
	 * @param width   The width of this component in scaled component space
	 * @param height  The height of this component in scaled component space
	 * @param mouseX  The mouse x coordinate in scaled component space - 0 is the left edge, width is the right edge.
	 * @param mouseY  The mouse y coordinate in scaled component space - 0 is the top edge, height is the bottom edge.
	 */
	@Environment(EnvType.CLIENT)
	public abstract void paintScaled(DrawContext context, int width, int height, int mouseX, int mouseY);
}
