package net.modfest.scatteredshards.client.screen.widget;

import io.github.cottonmc.cotton.gui.widget.WTextField;
import io.github.cottonmc.cotton.gui.widget.data.InputResult;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;

import java.util.function.Consumer;

public class WProtectableField extends WTextField {
	public WProtectableField() {
		this.setMaxLength(256);
	}

	public WProtectableField(Component suggestion) {
		super(suggestion);
		this.setMaxLength(256);
	}

	@Override
	public @NonNull InputResult onCharTyped(@NonNull CharacterEvent ch) {
		if (this.isEditable()) return super.onCharTyped(ch);
		return InputResult.IGNORED;
	}

	@Override
	protected void renderCursor(GuiGraphicsExtractor context, int x, int y, String visibleText) {
		if (this.isEditable()) super.renderCursor(context, x, y, visibleText);
	}

	@Override
	protected void renderSelection(GuiGraphicsExtractor context, int x, int y, String visibleText) {
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

	public WProtectableField setTextChangedListener(Consumer<Component> consumer) {
		super.setChangedListener((it) -> consumer.accept(Component.literal(it)));
		return this;
	}

	@Override
	public boolean canFocus() {
		return isEditable();
	}
}
