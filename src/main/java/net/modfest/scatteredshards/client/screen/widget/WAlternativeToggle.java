package net.modfest.scatteredshards.client.screen.widget;

import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.impl.client.NinePatchTextureRendererImpl;
import io.github.cottonmc.cotton.gui.widget.WWidget;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import io.github.cottonmc.cotton.gui.widget.data.InputResult;
import io.github.cottonmc.cotton.gui.widget.data.Rect2i;
import juuxel.libninepatch.NinePatch;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.ScatteredShards;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;

public class WAlternativeToggle extends WWidget {
	NinePatch<Identifier> button = NinePatch.builder(ScatteredShards.id("textures/gui/button.png"))
		.cornerSize(4)
		.cornerUv(4 / 200f, 4 / 20f)
		.mode(NinePatch.Mode.TILING)
		.build();

	NinePatch<Identifier> recessedButton = NinePatch.builder(ScatteredShards.id("textures/gui/button_recessed.png"))
		.cornerSize(4)
		.cornerUv(4 / 200f, 4 / 20f)
		.mode(NinePatch.Mode.TILING)
		.build();

	@Nullable
	protected Text leftLabel = Text.literal("Left Text");
	@Nullable
	protected Text rightLabel = Text.literal("Right Text");

	protected boolean isRight = false;
	protected Runnable onRight = () -> {
	};
	protected Runnable onLeft = () -> {
	};

	public WAlternativeToggle() {
	}

	public WAlternativeToggle(@Nullable Text left, @Nullable Text right) {
		this.leftLabel = left;
		this.rightLabel = right;
	}

	/* This is all just pretty much a flex/mini Either */
	public void ifLeft(Runnable r) {
		if (!isRight) r.run();
	}

	public void ifRight(Runnable r) {
		if (isRight) r.run();
	}

	/**
	 * Produces an Optional containing the function result only if the left alternative of this control is selected.
	 *
	 * @param <T>      The type of the function result
	 * @param function A function to apply to this component if the left side is selected.
	 * @return The function result; or empty if the right side is selected.
	 */
	public <T> Optional<T> mapLeft(Function<WAlternativeToggle, T> function) {
		return (isRight) ? Optional.empty() : Optional.of(function.apply(this));
	}

	/**
	 * Produces an Optional containing the function result only if the right alternative of this control is selected.
	 *
	 * @param <T>      The type of the function result
	 * @param function A function to apply to this component if the right side is selected.
	 * @return The function result; or empty if the left side is selected.
	 */
	public <T> Optional<T> mapRight(Function<WAlternativeToggle, T> function) {
		return (isRight) ? Optional.of(function.apply(this)) : Optional.empty();
	}

	public <T> Optional<T> mapLeft(Supplier<T> supplier) {
		return (isRight) ? Optional.empty() : Optional.of(supplier.get());
	}

	public <T> Optional<T> mapRight(Supplier<T> supplier) {
		return (isRight) ? Optional.of(supplier.get()) : Optional.empty();
	}

	/**
	 * Function version of {@link #map(Object, Object)}, returning either ifLeft or ifRight depending on which side is selected.
	 */
	public <T> T map(Function<WAlternativeToggle, T> ifLeft, Function<WAlternativeToggle, T> ifRight) {
		return (isRight) ? ifRight.apply(this) : ifLeft.apply(this);
	}

	/**
	 * Supplier version of {@link #map(Object, Object)}, returning either left or right depending on which side is selected.
	 */
	public <T> T map(Supplier<T> left, Supplier<T> right) {
		return (isRight) ? right.get() : left.get();
	}

	/**
	 * Maps this widget to one alternative or another based on whether the left or right side is visibly selected.
	 *
	 * @param <T>   The type of the return value.
	 * @param left  The alternative to map to if the left side is selected.
	 * @param right The alternative to map to if the right side is selected.
	 * @return Either left or right, depending on which side of the control is currently selected.
	 */
	public <T> T map(T left, T right) {
		return (isRight) ? right : left;
	}

	/* events - this is the important bit */

	public WAlternativeToggle onLeft(Runnable r) {
		this.onLeft = r;
		return this;
	}

	public WAlternativeToggle onRight(Runnable r) {
		this.onRight = r;
		return this;
	}

	public WAlternativeToggle setLeft() {
		if (isRight) this.onLeft.run();
		isRight = false;
		return this;
	}

	public WAlternativeToggle setRight() {
		if (!isRight) this.onRight.run();
		isRight = true;
		return this;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void paint(DrawContext context, int x, int y, int mouseX, int mouseY) {
		int halfWidth = (int) Math.ceil(this.width / 2.0);

		int hoverX = (isRight) ? 0 : halfWidth - 1;
		boolean hovered = (mouseX >= hoverX && mouseY >= 0 && mouseX < hoverX + halfWidth && mouseY < getHeight());

		MatrixStack matrices = context.getMatrices();
		matrices.push();
		matrices.translate(x, y, 0);
		NinePatch<Identifier> leftButton = map(recessedButton, button);
		NinePatch<Identifier> rightButton = map(button, recessedButton);
		leftButton.draw(NinePatchTextureRendererImpl.INSTANCE, context, halfWidth, this.getHeight());
		matrices.translate(halfWidth - 1, 0, 0);
		rightButton.draw(NinePatchTextureRendererImpl.INSTANCE, context, halfWidth, this.getHeight());

		matrices.pop();

		ScreenDrawing.drawStringWithShadow(context, leftLabel.asOrderedText(), HorizontalAlignment.CENTER, x + 2, y + 5, halfWidth - 4, 0xFF_FFFFFF);
		ScreenDrawing.drawStringWithShadow(context, rightLabel.asOrderedText(), HorizontalAlignment.CENTER, x + 2 + halfWidth, y + 5, halfWidth - 4, 0xFF_FFFFFF);

		if (hovered) {
			ScreenDrawing.drawBeveledPanel(context, x + hoverX, y, halfWidth, this.getHeight(), 0xFF_FFFFFF, 0x00_FFFFFF, 0xFF_FFFFFF);
		}
	}

	public Rect2i getActiveRect(int x, int y) {
		int halfWidth = (int) Math.ceil(this.width / 2.0);
		int xofs = (isRight) ? 0 : halfWidth - 1;
		return new Rect2i(x + xofs, y, halfWidth, getHeight());
	}

	public boolean hitActive(int x, int y) {
		int halfWidth = (int) Math.ceil(this.width / 2.0);
		int hoverX = (isRight) ? 0 : halfWidth - 1;
		return (x >= hoverX && y >= 0 && x < hoverX + halfWidth && y < getHeight());
	}

	@Override
	public InputResult onClick(int x, int y, int button) {
		if (hitActive(x, y) && button == 0) {
			isRight = !isRight;

			map(onLeft, onRight).run();

			return InputResult.PROCESSED;
		} else {
			return InputResult.IGNORED;
		}
	}

	@Override
	public boolean canResize() {
		return true;
	}
}
