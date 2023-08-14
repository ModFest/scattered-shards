package net.modfest.scatteredshards.client.screen.widget;

import io.github.cottonmc.cotton.gui.widget.WPanelWithInsets;
import io.github.cottonmc.cotton.gui.widget.WWidget;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import io.github.cottonmc.cotton.gui.widget.WLabel;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.component.ShardCollectionComponent;
import net.modfest.scatteredshards.component.ShardLibraryComponent;

import java.util.List;
import java.util.function.Consumer;
import java.util.ArrayList;

public class WShardSetPanel extends WPanelWithInsets {
	private static final int MINI_SHARD_WIDTH = 12;
	private static final int MINI_SHARD_HALFWIDTH = MINI_SHARD_WIDTH / 2;
	private static final int MINI_SHARD_HEIGHT = 16;
	
	protected Consumer<Shard> shardConsumer = (it) -> {};
	
	private WLabel sourceLabel = new WLabel(Text.literal(""));
	private List<WMiniShard> shards = new ArrayList<>();
	public WShardSetPanel() {
		this.setInsets(new Insets(2));
	}
	
	public WShardSetPanel setShardConsumer(Consumer<Shard> consumer) {
		this.shardConsumer = consumer;
		return this;
	}
	
	private void add(WWidget w, int x, int width, int height) {
		w.setSize(width, height);
		w.setLocation(x, 0);
		children.add(w);
	}
	
	public int layoutWidth() {
		return this.width - insets.left() - insets.right();
	}
	
	public int layoutHeight() {
		return this.height - insets.top() - insets.bottom();
	}
	
	public void setShardSet(Identifier set, ShardLibraryComponent library, ShardCollectionComponent collection) {
		List<Identifier> shardSet = List.copyOf(library.getShardSet(set));
		
		
		//Add/dump MiniShards till we have the same number of card icons as the shardSet has cards
		while(shards.size() > shardSet.size()) shards.remove(shards.size()-1);
		while(shards.size() < shardSet.size()) shards.add(new WMiniShard());
		
		//Start fresh on this panel's actual children
		this.children.clear();
		this.add(sourceLabel, 0, 100, 18);
		sourceLabel.setText(Shard.getSourceForSourceId(set));
		
		//The actual remaining layout width is less the label and the half-card hanging off the left and right sides
		int spaceRemaining = layoutWidth() - 100 - MINI_SHARD_HALFWIDTH - MINI_SHARD_HALFWIDTH;
		int spacePerShard = spaceRemaining - children.size();
		int xofs = 100;
		
		for(int i=0; i<Math.min(shards.size(), shardSet.size()); i++) {
			Identifier shardId = shardSet.get(i);
			WMiniShard widget = shards.get(i);
			widget.setShardConsumer(shardConsumer);
			collection.contains(shardId);
			widget.setShard(library.getShard(shardId), collection.contains(shardId));
			this.add(widget, xofs + (spacePerShard * i), MINI_SHARD_WIDTH, MINI_SHARD_HEIGHT);
		}
		
		if (host != null) this.validate(host);
	}
}
