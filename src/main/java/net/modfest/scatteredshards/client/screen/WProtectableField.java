package net.modfest.scatteredshards.client.screen;

import io.github.cottonmc.cotton.gui.widget.WTextField;
import io.github.cottonmc.cotton.gui.widget.data.InputResult;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.text.Text;

public class WProtectableField extends WTextField {
	public WProtectableField() {
	}
	
	public WProtectableField(Text suggestion) {
		super(suggestion);
	}
	
	@Override
	public InputResult onCharTyped(char ch) {
		if (this.isEditable()) return super.onCharTyped(ch);
		return InputResult.IGNORED;
	}
	
	@Override
	protected void renderCursor(GuiGraphics context, int x, int y, String visibleText) {
		if (this.isEditable()) super.renderCursor(context, x, y, visibleText);
	}
	
	@Override
	protected void renderSelection(GuiGraphics context, int x, int y, String visibleText) {
		if (this.isEditable()) super.renderSelection(context, x, y, visibleText);
	}
	
	@Override
	public WTextField setEditable(boolean editable) {
		super.setEditable(editable);
		
		if (!isEditable()) {
			this.setCursorPos(0);
			this.releaseFocus();
		}
		
		return this;
	}
	
	@Override
	public boolean canFocus() {
		return isEditable();
	}
}
