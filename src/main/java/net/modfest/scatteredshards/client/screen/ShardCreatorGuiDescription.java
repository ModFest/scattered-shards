package net.modfest.scatteredshards.client.screen;

import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import net.modfest.scatteredshards.core.api.shard.Shard;

public class ShardCreatorGuiDescription extends LightweightGuiDescription {

	public static class Screen extends CottonClientScreen {

		public Screen(Shard shard) {
			super(new ShardCreatorGuiDescription(shard));
		}

		public Screen() {
			this(Shard.empty());
		}
	}

	public ShardCreatorGuiDescription(Shard shard) {
		WGridPanel root = new WGridPanel(1);
		setRootPanel(root);
		root.setSize(256, 240);
		root.setInsets(Insets.ROOT_PANEL);

		root.add(new WShardPanel(shard, true), 128, 12);
		/*WSprite icon = new WSprite(new Identifier("minecraft:textures/item/redstone.png"));
		root.add(icon, 0, 2, 1, 1);

		WButton button = new WButton(Text.translatable("gui.examplemod.examplebutton"));
		root.add(button, 0, 3, 4, 1);

		WLabel label = new WLabel(Text.literal("Test"), 0xFFFFFF);
		root.add(label, 0, 4, 2, 1);*/

		root.validate(this);
	}
}
