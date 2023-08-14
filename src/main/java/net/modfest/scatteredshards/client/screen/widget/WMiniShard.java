package net.modfest.scatteredshards.client.screen.widget;

import java.util.function.Consumer;

import org.quiltmc.loader.api.minecraft.ClientOnly;

import com.mojang.blaze3d.systems.RenderSystem;

import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.WWidget;
import io.github.cottonmc.cotton.gui.widget.data.InputResult;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.api.shard.ShardType;

public class WMiniShard extends WWidget {
	
	protected Shard shard = null;
	protected ShardType shardType = null;
	protected boolean isCollected = false;
	
	protected Consumer<Shard> shardConsumer = (it) -> {};
	
	public WMiniShard() {}
	
	public WMiniShard setShard(Shard shard, boolean collected) {
		this.shard = shard;
		this.shardType = shard.getShardType();
		this.isCollected = collected;
		return this;
	}
	
	public WMiniShard setShardConsumer(Consumer<Shard> onClick) {
		this.shardConsumer = onClick;
		return this;
	}
	
	@ClientOnly
	@Override
	public void paint(GuiGraphics context, int x, int y, int mouseX, int mouseY) {
		Identifier tex = (isCollected) ? shardType.getMiniFrontTexture() : shardType.getMiniBackTexture();
		ScreenDrawing.texturedRect(context, x, y, 12, 16, tex, 0xFF_FFFFFF);
		if (isCollected) {
			//Maybe draw a teeny tiny icon
			shard.icon().ifLeft((it) -> {
				context.getMatrices().push();
				context.getMatrices().translate(6, 6, 0);
				context.getMatrices().scale(1/3f, 1/3f, 1); // 18px -> 6px or 16px -> 5.33px
				RenderSystem.enableDepthTest();
				context.drawItemWithoutEntity(it, 0, 0);
				context.getMatrices().pop();
			});
			shard.icon().ifRight((it) -> {
				ScreenDrawing.texturedRect(context, x+3, y+3, 6, 6, it, 0xFF_FFFFFF);
			});
		}
	}
	
	@Override
	public InputResult onClick(int x, int y, int button) {
		if (button == 0) {
			shardConsumer.accept(shard);
			return InputResult.PROCESSED;
		} else {
			return InputResult.IGNORED;
		}
	}
	
	@Override
	public int getWidth() {
		return 12;
	}
	
	@Override
	public int getHeight() {
		return 16;
	}
	
	@Override
	public boolean canResize() {
		return false;
	}
}
