package net.modfest.scatteredshards.client.screen.widget;

import java.util.function.Consumer;

import com.mojang.blaze3d.systems.RenderSystem;

import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.WWidget;
import io.github.cottonmc.cotton.gui.widget.data.InputResult;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.api.shard.ShardType;

public class WMiniShard extends WWidget {
	private static final Identifier MINI_OUTLINE = ScatteredShards.id("/textures/gui/shards/mini_outline.png");
	
	protected Shard shard = null;
	protected ShardType shardType = null;
	protected boolean isCollected = false;
	
	protected Consumer<Shard> shardConsumer = (it) -> {};
	
	public WMiniShard() {}
	
	public WMiniShard setShard(Shard shard, boolean collected) {
		this.shard = shard;
		this.shardType = ScatteredShardsAPI.getClientLibrary().shardTypes().get(shard.shardTypeId()).orElse(ShardType.MISSING);
		this.isCollected = collected;
		return this;
	}
	
	public WMiniShard setShardConsumer(Consumer<Shard> onClick) {
		this.shardConsumer = onClick;
		return this;
	}
	
	@Environment(EnvType.CLIENT)
	@Override
	public void paint(DrawContext context, int x, int y, int mouseX, int mouseY) {
		Identifier tex = (isCollected) ? ShardType.getMiniFrontTexture(shard.shardTypeId()) : ShardType.getMiniBackTexture(shard.shardTypeId());
		int color = (isCollected) ? 0xFF_FFFFFF : 0xFF_668866;
		float opacity = (isCollected) ? 1.0f : 0.6f;
		ScreenDrawing.texturedRect(context, x, y, 12, 16, tex, color, opacity);
		if (isCollected) {
			//Maybe draw a teeny tiny icon
			shard.icon().ifLeft((it) -> {
				context.getMatrices().push();
				context.getMatrices().translate(x + 3, y + 3, 0);
				context.getMatrices().scale(0.375f, 0.375f, 1); // 16px -> 6px
				RenderSystem.enableDepthTest();
				context.drawItemWithoutEntity(it, 0, 0);
				context.getMatrices().pop();
			});
			shard.icon().ifRight((it) -> {
				ScreenDrawing.texturedRect(context, x+3, y+3, 6, 6, it, 0xFF_FFFFFF);
			});
		}
		
		boolean hovered = (mouseX>=0 && mouseY>=0 && mouseX<getWidth() && mouseY<getHeight());
		if (hovered) {
			ScreenDrawing.texturedRect(context, x - 2, y - 2, 16, 20, MINI_OUTLINE, 0, 0, 1, 1, 0xFF_FFFFFF);
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
