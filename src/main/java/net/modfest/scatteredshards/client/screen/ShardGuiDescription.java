package net.modfest.scatteredshards.client.screen;

import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WPlainPanel;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import net.modfest.scatteredshards.api.shard.Shard;

public abstract class ShardGuiDescription extends LightweightGuiDescription {

	protected final WPlainPanel root;
	protected final WShardPanel shardView;

	private ShardGuiDescription(WShardPanel panel) {
		this.root = new WPlainPanel();
		this.shardView = panel;

		setRootPanel(root);
		root.setSize(300, 200);
		root.setInsets(Insets.ROOT_PANEL);

		root.add(panel, 176, -7);
		root.validate(this);
	}

	public ShardGuiDescription(Shard shard) {
		this(new WShardPanel(shard));
	}

	public ShardGuiDescription() {
		this(new WShardPanel());
	}
}
