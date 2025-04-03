package net.modfest.scatteredshards.client;

import com.mojang.datafixers.util.Either;
import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.item.ItemStack;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.api.shard.ShardType;
import net.modfest.scatteredshards.util.ModMetaUtil;

import java.util.ArrayList;
import java.util.List;

public class ShardCollectedToast implements Toast {
	public static final int TITLE_COLOR = 0xFF_FFFF00;
	public static final Text TITLE = Text.translatable("toast.scattered_shards.collected");
	private static final Identifier TEXTURE = Identifier.ofVanilla("toast/advancement");
	public static final int DURATION = 5000;

	Either<ItemStack, Identifier> icon;
	List<OrderedText> descLines;
	List<OrderedText> hintLines;
	private final int height;

	public ShardCollectedToast(Shard shard) {
		
		Text hint;
		
		if (ScatteredShardsClient.VIEW_COLLECTION.isUnbound()) {
			hint = Text.translatable(
					"toast.scattered_shards.collected.prompt_without_key",
					Text.literal("/shards").formatted(Formatting.AQUA).formatted(Formatting.BOLD)
					);
		} else {
			hint = Text.translatable(
					"toast.scattered_shards.collected.prompt",
					Text.keybind(ScatteredShardsClient.VIEW_COLLECTION.getTranslationKey()).formatted(Formatting.GOLD).formatted(Formatting.BOLD)
					);
		}
		
		this.icon = shard.icon();
		this.descLines = wrap(List.of(shard.name().copy().withColor(ScatteredShardsAPI.getClientLibrary().shardTypes().get(shard.shardTypeId()).orElse(ShardType.MISSING).textColor())));
		this.hintLines = wrap(List.of(hint));
		this.height = 32 + Math.max(0, Math.max(this.descLines.size(), this.hintLines.size()) - 1) * 11;
		icon.ifRight(ModMetaUtil::touchIconTexture);
	}

	@Override
	public Visibility draw(DrawContext graphics, ToastManager manager, long startTime) {
		graphics.drawGuiTexture(TEXTURE, 0, 0, this.getWidth(), this.getHeight());
		TextRenderer textRenderer = manager.getClient().textRenderer;

		graphics.drawText(
			textRenderer,
			TITLE, 32, 7, TITLE_COLOR,
			false
		);

		double time = DURATION * manager.getNotificationDisplayTimeMultiplier();

		List<OrderedText> body = startTime >= (time / 2) && !hintLines.isEmpty() ? hintLines : descLines;

		for (int i = 0; i < body.size(); i++) {
			graphics.drawText(textRenderer, body.get(i), 32, 18 + i * 11, 0xFF_FFFFFF, false);
		}

		icon.ifLeft(it -> graphics.drawItemWithoutEntity(it, 8, 8));
		icon.ifRight(it -> ScreenDrawing.texturedRect(graphics, 8, 8, 16, 16, it, 0xFF_FFFFFF));
		return startTime >= time ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
	}

	private List<OrderedText> wrap(List<Text> messages) {
		List<OrderedText> list = new ArrayList<>();
		messages.forEach(text -> list.addAll(MinecraftClient.getInstance().textRenderer.wrapLines(text, getWidth() - 40)));
		return list;
	}

	@Override
	public int getHeight() {
		return height;
	}
}
