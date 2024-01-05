package net.modfest.scatteredshards.client.screen.widget;

import io.github.cottonmc.cotton.gui.widget.WPanelWithInsets;
import io.github.cottonmc.cotton.gui.widget.WWidget;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.api.ShardCollection;
import net.modfest.scatteredshards.api.ShardLibrary;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.api.shard.ShardType;
import net.modfest.scatteredshards.client.screen.widget.scalable.WScaledLabel;

import java.util.List;
import java.util.function.Consumer;
import java.util.ArrayList;

public class WShardSetPanel extends WPanelWithInsets {
	private static final int MINI_SHARD_WIDTH = 12;
	//private static final int MINI_SHARD_HALFWIDTH = MINI_SHARD_WIDTH / 2;
	private static final int MINI_SHARD_HEIGHT = 16;
	
	protected Consumer<Shard> shardConsumer = (it) -> {};
	
	private WScaledLabel sourceLabel = new WScaledLabel(Text.literal(""), 0.8f)
			.setColor(0xFF_CCFFCC)
			.setShadow(true);
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
		w.setParent(this);
	}
	
	public int layoutWidth() {
		return this.width - insets.left() - insets.right();
	}
	
	public int layoutHeight() {
		return this.height - insets.top() - insets.bottom();
	}
	
	public void setShardSet(Identifier setId, ShardLibrary library, ShardCollection collection) {
		List<Identifier> shardSet = new ArrayList<>();
		shardSet.addAll(library.shardSets().get(setId));
		shardSet.sort((a, b) -> {
			int aPriority = library.shards().get(a)
					.map(it -> it.shardTypeId())
					.flatMap(library.shardTypes()::get)
					.map(ShardType::listOrder)
					.orElse(Integer.MAX_VALUE);
			
			int bPriority = library.shards().get(b)
					.map(it -> it.shardTypeId())
					.flatMap(library.shardTypes()::get)
					.map(ShardType::listOrder)
					.orElse(Integer.MAX_VALUE);
			
			return Integer.compare(aPriority, bPriority);
		});
		
		//Add/dump MiniShards till we have the same number of card icons as the shardSet has cards
		while(shards.size() > shardSet.size()) shards.remove(shards.size()-1);
		while(shards.size() < shardSet.size()) shards.add(new WMiniShard());
		
		//Start fresh on this panel's actual children
		this.children.clear();
		this.add(sourceLabel, 0, 96, 18);
		sourceLabel.setLocation(0+this.insets.left(), 2+this.insets.top());
		sourceLabel.setText(Shard.getSourceForSourceId(setId));
		
		//The actual remaining layout width is less the label and the width of the card itself
		int spaceRemaining = layoutWidth() - 100 - MINI_SHARD_WIDTH;
		int spacePerShard = spaceRemaining / shardSet.size();
		int xofs = 100;
		
		for(int i=0; i<Math.min(shards.size(), shardSet.size()); i++) {
			Identifier shardId = shardSet.get(i);
			WMiniShard widget = shards.get(i);
			widget.setShardConsumer(shardConsumer);
			collection.contains(shardId);
			widget.setShard(library.shards().get(shardId).orElse(Shard.MISSING_SHARD), collection.contains(shardId));
			this.add(widget, xofs + (spacePerShard * i), MINI_SHARD_WIDTH, MINI_SHARD_HEIGHT);
		}
		
		if (host != null) this.validate(host);
	}
	
	/*
	//TODO: Replace with json / ShardType field
	private static int shardPriority(String path) {
		return switch(path) {
		case "scattered_shards_visitor" -> 0;
		case "scattered_shards_challenge" -> 1;
		case "scattered_shards_secret" -> 2;
		default -> Integer.MAX_VALUE;
		};
	}
	
	private static int shardComparator(Identifier a, Identifier b) {
		
		return Integer.compare(shardPriority(a.getPath()), shardPriority(b.getPath()));
	}*/
}
