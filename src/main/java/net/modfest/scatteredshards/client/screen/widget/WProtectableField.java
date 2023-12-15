package net.modfest.scatteredshards.client.screen.widget;

import java.util.function.Consumer;

import io.github.cottonmc.cotton.gui.widget.WTextField;
import io.github.cottonmc.cotton.gui.widget.data.InputResult;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;

public class WProtectableField extends WTextField {
	public WProtectableField() {
		this.setMaxLength(256);
	}
	
	public WProtectableField(Text suggestion) {
		super(suggestion);
		this.setMaxLength(256);
	}
	
	@Override
	public InputResult onCharTyped(char ch) {
		if (this.isEditable()) return super.onCharTyped(ch);
		return InputResult.IGNORED;
	}
	
	@Override
	protected void renderCursor(DrawContext context, int x, int y, String visibleText) {
		if (this.isEditable()) super.renderCursor(context, x, y, visibleText);
	}
	
	@Override
	protected void renderSelection(DrawContext context, int x, int y, String visibleText) {
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
	public WProtectableField setMaxLength(int max) {
		super.setMaxLength(max);
		return this;
	}
	
	public WProtectableField setChangedListener(Consumer<String> consumer) {
		super.setChangedListener(consumer);
		return this;
	}
	
	public WProtectableField setTextChangedListener(Consumer<Text> consumer) {
		super.setChangedListener((it) -> consumer.accept(Text.literal(it)));
		return this;
	}
	
	@Override
	public boolean canFocus() {
		return isEditable();
	}
}
