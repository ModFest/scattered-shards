package net.modfest.scatteredshards.client.screen.widget;

import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import io.github.cottonmc.cotton.gui.widget.TooltipBuilder;
import io.github.cottonmc.cotton.gui.widget.WWidget;
import io.github.cottonmc.cotton.gui.widget.data.InputResult;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import net.modfest.scatteredshards.ScatteredShards;
import net.modfest.scatteredshards.api.GlobalCollection;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.api.shard.ShardIconOffsets;
import net.modfest.scatteredshards.api.shard.ShardTextureSettings;
import net.modfest.scatteredshards.api.shard.ShardType;
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
	private int width = (int)ShardTextureSettings.Size.DEFAULT_MINI.width();
	private int height = (int)ShardTextureSettings.Size.DEFAULT_MINI.height();

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
		ShardTextureSettings.Size size = shardType.getTextureSettings().getMiniSize();
		this.width = (int)size.width();
		this.height = (int)size.height();

		return this;
	}

	public WMiniShard setShardConsumer(Consumer<Shard> onClick) {
		this.shardConsumer = onClick;
		return this;
	}

	@Environment(EnvType.CLIENT)
	@Override
	public void paint(GuiGraphics context, int x, int y, int mouseX, int mouseY) {
		Identifier tex = (isCollected) ? ShardType.getMiniFrontTexture(shard.shardTypeId()) : ShardType.getMiniBackingTexture(shard.shardTypeId());
		int color = (isCollected) ? 0xFF_FFFFFF : 0xFF_668866;
		float opacity = (isCollected) ? 1.0f : 0.6f;
		ScreenDrawing.texturedRect(context, x, y, getWidth(), getHeight(), tex, color, opacity);
		if (isCollected && ScatteredShardsAPI.getClientLibrary().shardDisplaySettings().drawMiniIcons()) {
			//Maybe draw a teeny tiny icon

			ShardIconOffsets.Offset offset = this.shardType.getOffsets().getMini();
			shard.icon().ifLeft((it) -> {
				context.pose().pushMatrix();
				context.pose().translate(x + offset.left(), y + offset.up());
				context.pose().scale(0.5f, 0.5f); // 16px -> 8px
				context.renderFakeItem(it, 0, 0);
				context.pose().popMatrix();
			});
			shard.icon().ifRight((it) -> ScreenDrawing.texturedRect(context, x + offset.left(), y + offset.up(), 8, 8, it, 0xFF_FFFFFF));
		}

		boolean hovered = (mouseX >= 0 && mouseY >= 0 && mouseX < getWidth() && mouseY < getHeight());
		if (hovered) {
			ScreenDrawing.texturedRect(context, x - 2, y - 2, getWidth()+4, getHeight()+4, MINI_OUTLINE, 0, 0, 1, 1, 0xFF_FFFFFF);

			renderTooltip(context, x, y, mouseX, mouseY);
		} else if ( // Awful bullshit write real code later
			Minecraft.getInstance().screen instanceof ShardTabletGuiDescription.Screen stgds
				&& stgds.getDescription().getRootPanel() instanceof WLeftRightPanel wlrp
				&& wlrp.rightPanel instanceof WShardPanel wsp
				&& wsp.getShard() == shard
		) {
			ScreenDrawing.texturedRect(context, x - 2, y - 2, getWidth()+4, getHeight()+4, MINI_OUTLINE_SLIGHT, 0, 0, 1, 1, 0xFF_FFFFFF);
		}
	}

	@Override
	public void addTooltip(TooltipBuilder tooltip) {

		if (!shard.name().getString().isBlank()) {
			tooltip.add(shard.name());
		}
		tooltip.add(ShardType.getDescription(shard.shardTypeId()).copy().withColor(0xFF_000000 | shardType.textColor()));
		GlobalCollection globalCollection = ScatteredShardsAPI.getClientGlobalCollection();
		if (globalCollection != null) {
			tooltip.add(Component.translatable("gui.scattered_shards.tablet.tooltip.global_collection", "%.1f%%".formatted(100 * globalCollection.getCount(shardId) / (float) globalCollection.totalPlayers())).withStyle(ChatFormatting.GRAY));
		}

		super.addTooltip(tooltip);
	}

	@Override
	public InputResult onClick(int x, int y, int button) {
		if (button == 0) {
			Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), 1.0f, 0.25f));
			shardConsumer.accept(shard);
			return InputResult.PROCESSED;
		} else {
			return InputResult.IGNORED;
		}
	}

	@Override
	public int getWidth() {
		return width;
	}

	@Override
	public int getHeight() {
		return height;
	}
}
