package net.modfest.scatteredshards.client.screen.widget.scalable;

import java.util.function.Supplier;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Either;

import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Identifier;

public class WShardIcon extends WScalableWidget {
	protected Supplier<Either<ItemStack, Identifier>> icon = () -> Either.left(ItemStack.EMPTY);

	public WShardIcon(float scale) {
		this.scale = scale;
	}

	public WShardIcon setIcon(Either<ItemStack, Identifier> icon) {
		this.icon = () -> icon;
		return this;
	}

	public WShardIcon setIcon(Supplier<Either<ItemStack, Identifier>> icon) {
		this.icon = icon;
		return this;
	}

	public WShardIcon setIcon(ItemStack itemStack) {
		this.icon = () -> Either.left(itemStack);
		return this;
	}

	public WShardIcon setIcon(Identifier image) {
		this.icon = () -> Either.right(image);
		return this;
	}

	@Override
	public void paintScaled(DrawContext context, int width, int height, int mouseX, int mouseY) {
		icon.get().ifLeft(it -> {
			RenderSystem.enableDepthTest();
			context.drawItemWithoutEntity(it, width / 2 - 8, height / 2 - 8);
		});
		icon.get().ifRight(it -> {
			ScreenDrawing.texturedRect(context, 0, 0, width, height, it, 0xFF_FFFFFF);
		});
	}
	
	
	@Override
	public boolean canResize() {
		return true;
	}
}
