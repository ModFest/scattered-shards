package net.modfest.scatteredshards.client.screen;

import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import net.modfest.scatteredshards.api.shard.Shard;

public class ShardCreatorGuiDescription extends LightweightGuiDescription {

	public static class Screen extends CottonClientScreen {

		public Screen(Shard shard) {
			super(new ShardCreatorGuiDescription(shard));
		}

		public Screen() {
			this(Shard.MISSING_SHARD);
		}
	}

	public ShardCreatorGuiDescription(Shard shard) {
		WGridPanel root = new WGridPanel(1);
		setRootPanel(root);
		root.setSize(300, 200);
		root.setInsets(Insets.ROOT_PANEL);

		root.add(new WShardPanel(shard, true), 128, 12);
		root.validate(this);
	}
}
