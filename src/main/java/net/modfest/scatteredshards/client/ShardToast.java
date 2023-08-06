package net.modfest.scatteredshards.client;

import java.util.List;

import io.github.cottonmc.cotton.gui.client.ScreenDrawing;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.sound.PositionedSoundInstance;
import net.minecraft.client.toast.Toast;
import net.minecraft.client.toast.ToastManager;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.OrderedText;
import net.minecraft.text.Text;
import net.modfest.scatteredshards.ScatteredShardsContent;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.api.shard.ShardType;

public class ShardToast implements Toast {
	public static final int YELLOW = 0xFF_FFFF00;
	public static final int WHITE = 0xFF_FFFFFF;
	public static final Text COLLECTED_TEXT = Text.translatable("scattered_shards.toast.collect");
	
	public static final int DURATION = 5000;
	private final Shard shard;
	private boolean soundPlayed;
	
	public ShardToast(Shard shard) {
		this.shard = shard;
	}
	
	@SuppressWarnings("resource")
	@Override
	public Visibility draw(GuiGraphics graphics, ToastManager manager, long startTime) {
		graphics.drawTexture(TEXTURE, 0, 0, 0, 0, this.getWidth(), this.getHeight());
		TextRenderer textRenderer = manager.getGame().textRenderer;
		
		if (shard != null) {
			List<OrderedText> lines = manager.getGame().textRenderer.wrapLines(shard.name(), 125); // 160 is the total toast width so this is reasonable
			if (lines.size() == 1) {
				graphics.drawText(textRenderer, COLLECTED_TEXT, 30, 7, YELLOW, false);
				graphics.drawText(textRenderer, shard.name(), 30, 18, shard.shardType().textColor(), false);
			} else {
				int y = this.getHeight() / 2 - lines.size() * 9 / 2;

				for(OrderedText orderedText : lines) {
					graphics.drawText(manager.getGame().textRenderer, orderedText, 30, y, shard.shardType().textColor(), false);
					y += 9;
				}
			}

			if (!this.soundPlayed && startTime > 0L) {
				this.soundPlayed = true;
				SoundEvent collectSound = null;
				if (shard.shardType() == ShardType.VISITOR) collectSound = ScatteredShardsContent.COLLECT_VISITOR;
				if (shard.shardType() == ShardType.CHALLENGE) collectSound = ScatteredShardsContent.COLLECT_CHALLENGE;
				if (shard.shardType() == ShardType.SECRET) collectSound = SoundEvents.UI_TOAST_CHALLENGE_COMPLETE;
				
				if (collectSound != null) manager.getGame().getSoundManager().play(PositionedSoundInstance.master(collectSound, 1.0F, 0.8F));
			}
			
			shard.icon().ifLeft(it -> {
				graphics.drawItemWithoutEntity(it, 8, 8);
			});
			
			shard.icon().ifRight(it -> {
				ScreenDrawing.texturedRect(graphics, 8, 8, 16, 16, it, 0xFF_FFFFFF);
			});
			return (double)startTime >= 5000.0 * manager.method_48221() ? Toast.Visibility.HIDE : Toast.Visibility.SHOW;
		} else {
			return Toast.Visibility.HIDE;
		}
	}
}
