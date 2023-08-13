package net.modfest.scatteredshards.client.screen;

import io.github.cottonmc.cotton.gui.client.BackgroundPainter;
import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WPlainPanel;
import io.github.cottonmc.cotton.gui.widget.WScrollBar;
import io.github.cottonmc.cotton.gui.widget.data.Axis;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.client.screen.widget.WLeftRightPanel;
import net.modfest.scatteredshards.client.screen.widget.WShardPanel;
import net.modfest.scatteredshards.component.ShardCollectionComponent;
import net.modfest.scatteredshards.component.ShardLibraryComponent;

import java.util.Collection;

public class ShardTabletGuiDescription extends LightweightGuiDescription {
	public static final int ROWS_PER_SCREEN = 5;
	
	protected final ShardCollectionComponent collection;
	protected final ShardLibraryComponent library;
	
	WShardPanel shardPanel = new WShardPanel();
	WScrollBar shardSelectorScrollBar = new WScrollBar(Axis.VERTICAL);
	WPlainPanel shardSelector = new WPlainPanel();
	
	public ShardTabletGuiDescription(ShardCollectionComponent collection, ShardLibraryComponent library) {
		this.collection = collection;
		this.library = library;
		WLeftRightPanel root = new WLeftRightPanel(shardSelector, shardPanel);
		root.add(shardSelector, 0, 0, 154, 188);
		root.add(shardSelectorScrollBar, 154, 0, 18, 188);
		
		//TODO: Populate shards from collection
		int rows = ScatteredShardsAPI.getShardSets().size();
		int maxScroll = rows - ROWS_PER_SCREEN;
		if (maxScroll < 0) maxScroll = 0;
		shardSelectorScrollBar.setMaxValue(maxScroll);
		int curRow = 0;
		for(Identifier setId : ScatteredShardsAPI.getShardSets().keys()) {
			Collection<Shard> set = ScatteredShardsAPI.getShardSets().get(setId);
			int curCol = 0;
			int maxCol = set.size();
			for(Shard s : set) {
				Identifier shardId = ScatteredShardsAPI.getShardData().inverse().get(s);
				if (collection.contains(shardId)) {
					//set this cell to shard 's'
				} else {
					//set this cell to uncollected shard-back of 's''s ShardType
				}
				curCol++;
			}
			
			curRow++;
		}
		
		root.validate(this);
	}
	
	@Override
	public void addPainters() {
		shardSelector.setBackgroundPainter(BackgroundPainter.createColorful(0xFF_777777));
		super.addPainters();
	}
	
	public static class Screen extends CottonClientScreen {
		public Screen(ShardCollectionComponent collection, ShardLibraryComponent library) {
			super(new ShardTabletGuiDescription(collection, library));
		}
	}
}
