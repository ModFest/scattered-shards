package net.modfest.scatteredshards.client.screen.widget.scalable;

import org.quiltmc.loader.api.minecraft.ClientOnly;

import io.github.cottonmc.cotton.gui.widget.WWidget;
import net.minecraft.client.gui.GuiGraphics;

public abstract class WScalableWidget extends WWidget {
	
	protected float scale = 1.0f;
	
	@ClientOnly
	@Override
	public void paint(GuiGraphics context, int x, int y, int mouseX, int mouseY) {
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
	@ClientOnly
	public abstract void paintScaled(GuiGraphics context, int width, int height, int mouseX, int mouseY);
}
