package net.modfest.scatteredshards.client.screen.widget;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.TooltipBuilder;
import io.github.cottonmc.cotton.gui.widget.WWidget;
import io.github.cottonmc.cotton.gui.widget.data.InputResult;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.GlobalCollection;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.api.shard.ShardIconOffsets;
import net.modfest.scatteredshards.api.shard.ShardType;
import net.modfest.scatteredshards.client.ScatteredShardsClient;
import net.modfest.scatteredshards.client.screen.ShardTabletGuiDescription;
import net.modfest.scatteredshards.util.ModMetaUtil;

import java.util.function.Consumer;

public class WMiniShard extends WWidget {
	private static final Identifier MINI_OUTLINE = ScatteredShards.id("textures/gui/shards/mini_outline.png");
	private static final Identifier MINI_OUTLINE_SLIGHT = ScatteredShards.id("textures/gui/shards/mini_outline_slight.png");

	protected Shard shard = null;
	protected ShardType shardType = null;
	protected boolean isCollected = false;
	protected Identifier shardId;

	protected Consumer<Shard> shardConsumer = (it) -> {
	};

	public WMiniShard() {
	}

	public WMiniShard setShard(Shard shard, boolean collected, Identifier shardId) {
		shard.icon().ifRight(ModMetaUtil::touchIconTexture);
		this.shard = shard;
		this.shardType = ScatteredShardsAPI.getClientLibrary().shardTypes().get(shard.shardTypeId()).orElse(ShardType.MISSING);
		this.isCollected = collected;
		this.shardId = shardId;

		return this;
	}

	public WMiniShard setShardConsumer(Consumer<Shard> onClick) {
		this.shardConsumer = onClick;
		return this;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void paint(DrawContext context, int x, int y, int mouseX, int mouseY) {
		Identifier tex = (isCollected) ? ShardType.getMiniFrontTexture(shard.shardTypeId()) : ShardType.getMiniBackingTexture(shard.shardTypeId());
		int color = (isCollected) ? 0xFF_FFFFFF : 0xFF_668866;
		float opacity = (isCollected) ? 1.0f : 0.6f;
		ScreenDrawing.texturedRect(context, x, y, 12, 16, tex, color, opacity);
		if (isCollected && ScatteredShardsAPI.getClientLibrary().shardDisplaySettings().drawMiniIcons()) {
			//Maybe draw a teeny tiny icon

			ShardIconOffsets.Offset offset = this.shardType.getOffsets().getMini();
			shard.icon().ifLeft((it) -> {
				context.getMatrices().push();
				context.getMatrices().translate(x + offset.left(), y + offset.up(), 0);
				context.getMatrices().scale(0.5f, 0.5f, 1); // 16px -> 8px
				RenderSystem.enableDepthTest();
				context.drawItemWithoutEntity(it, 0, 0);
				context.getMatrices().pop();
			});
			shard.icon().ifRight((it) -> ScreenDrawing.texturedRect(context, x + offset.left(), y + offset.up(), 8, 8, it, 0xFF_FFFFFF));
		}

		boolean hovered = (mouseX >= 0 && mouseY >= 0 && mouseX < getWidth() && mouseY < getHeight());
		if (hovered) {
			ScreenDrawing.texturedRect(context, x - 2, y - 2, 16, 20, MINI_OUTLINE, 0, 0, 1, 1, 0xFF_FFFFFF);

			renderTooltip(context, x, y, mouseX, mouseY);
		} else if ( // Awful bullshit write real code later
			MinecraftClient.getInstance().currentScreen instanceof ShardTabletGuiDescription.Screen stgds
				&& stgds.getDescription().getRootPanel() instanceof WLeftRightPanel wlrp
				&& wlrp.rightPanel instanceof WShardPanel wsp
				&& wsp.getShard() == shard
		) {
			ScreenDrawing.texturedRect(context, x - 2, y - 2, 16, 20, MINI_OUTLINE_SLIGHT, 0, 0, 1, 1, 0xFF_FFFFFF);
		}
	}

	@Override
	public void addTooltip(TooltipBuilder tooltip) {

		if (ScatteredShardsClient.hasShiftDown() && !shard.name().getString().isBlank()) {
			tooltip.add(shard.name());
		}
		tooltip.add(ShardType.getDescription(shard.shardTypeId()).copy().withColor(shardType.textColor()));
		if (ScatteredShardsClient.hasShiftDown()) {
			GlobalCollection globalCollection = ScatteredShardsAPI.getClientGlobalCollection();
			if (globalCollection != null) {
				tooltip.add(Text.translatable("gui.scattered_shards.tablet.tooltip.global_collection", "%.1f%%".formatted(100 * globalCollection.getCount(shardId) / (float) globalCollection.totalPlayers())).formatted(Formatting.GRAY));
			}
		}

		super.addTooltip(tooltip);
	}

	@Override
	public InputResult onClick(int x, int y, int button) {
		if (button == 0) {
			MinecraftClient.getInstance().getSoundManager().play(PositionedSoundInstance.master(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 0.25f));
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
}
