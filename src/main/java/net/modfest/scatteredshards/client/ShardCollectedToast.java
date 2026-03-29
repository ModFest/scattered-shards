package net.modfest.scatteredshards.client;

import com.mojang.datafixers.util.Either;
import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.gui.components.toasts.Toast;
import net.minecraft.client.gui.components.toasts.ToastManager;
import net.minecraft.world.item.ItemStack;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.resources.Identifier;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.api.shard.ShardType;
import net.modfest.scatteredshards.util.ModMetaUtil;

import java.util.ArrayList;
import java.util.List;

public class ShardCollectedToast implements Toast {
	public static final int TITLE_COLOR = 0xFF_FFFF00;
	public static final Component TITLE = Component.translatable("toast.scattered_shards.collected");
	private static final Identifier TEXTURE = Identifier.withDefaultNamespace("toast/advancement");
	public static final int DURATION = 5000;

	Either<ItemStack, Identifier> icon;
	List<FormattedCharSequence> descLines;
	List<FormattedCharSequence> hintLines;
	private final int height;
	private double displayTimeMultiplier = 1;

	Visibility visibility = Visibility.HIDE;


	public ShardCollectedToast(Shard shard) {
		Component hint;

		if (ScatteredShardsClient.VIEW_COLLECTION.isUnbound()) {
			hint = Component.translatable(
				"toast.scattered_shards.collected.prompt_without_key",
				Component.literal("/shards").withStyle(ChatFormatting.AQUA).withStyle(ChatFormatting.BOLD)
			);
		} else {
			hint = Component.translatable(
				"toast.scattered_shards.collected.prompt",
				Component.keybind(ScatteredShardsClient.VIEW_COLLECTION.getName()).withStyle(ChatFormatting.GOLD).withStyle(ChatFormatting.BOLD)
			);
		}

		this.icon = shard.icon();
		this.descLines = wrap(List.of(shard.name().copy().withColor(0xFF_000000 | ScatteredShardsAPI.getClientLibrary().shardTypes().get(shard.shardTypeId()).orElse(ShardType.MISSING).textColor())));
		this.hintLines = wrap(List.of(hint));
		this.height = 32 + Math.max(0, Math.max(this.descLines.size(), this.hintLines.size()) - 1) * 11;
		icon.ifRight(ModMetaUtil::touchIconTexture);
	}

	private List<FormattedCharSequence> wrap(List<Component> messages) {
		List<FormattedCharSequence> list = new ArrayList<>();
		messages.forEach(text -> list.addAll(Minecraft.getInstance().font.split(text, width() - 40)));
		return list;
	}

	@Override
	public Visibility getWantedVisibility() {
		return visibility;
	}

	@Override
	public void update(ToastManager manager, long time) {
		displayTimeMultiplier = manager.getNotificationDisplayTimeMultiplier();
		this.visibility = (double) time < (double) DURATION * displayTimeMultiplier ? Visibility.SHOW : Visibility.HIDE;
	}

	@Override
	public void extractRenderState(GuiGraphicsExtractor graphics, Font font, long fullyVisibleForMs) {
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, TEXTURE, 0, 0, this.width(), this.height());

		graphics.text(
			font,
			TITLE, 32, 7, TITLE_COLOR,
			false
		);

		double time = DURATION * displayTimeMultiplier;

		// TODO: Check if this really is fullyVisibleForMS (it was named startTime before)
		List<FormattedCharSequence> body = fullyVisibleForMs >= (time / 2) && !hintLines.isEmpty() ? hintLines : descLines;

		for (int i = 0; i < body.size(); i++) {
			graphics.text(font, body.get(i), 32, 18 + i * 11, 0xFF_FFFFFF, false);
		}

		icon.ifLeft(it -> graphics.fakeItem(it, 8, 8));
		icon.ifRight(it -> ScreenDrawing.texturedRect(graphics, 8, 8, 16, 16, it, 0xFF_FFFFFF));
	}

	@Override
	public int height() {
		return height;
	}
}
