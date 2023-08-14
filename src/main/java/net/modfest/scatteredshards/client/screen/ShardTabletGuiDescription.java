package net.modfest.scatteredshards.client.screen;

import java.util.Collection;

import io.github.cottonmc.cotton.gui.client.BackgroundPainter;
import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WPlainPanel;
import io.github.cottonmc.cotton.gui.widget.WScrollBar;
import io.github.cottonmc.cotton.gui.widget.data.Axis;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import net.minecraft.util.Identifier;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.client.screen.widget.WLeftRightPanel;
import net.modfest.scatteredshards.client.screen.widget.WShardPanel;
import net.modfest.scatteredshards.client.screen.widget.WShardSetPanel;
import net.modfest.scatteredshards.component.ShardCollectionComponent;
import net.modfest.scatteredshards.component.ShardLibraryComponent;

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
		
		shardPanel.setShard(Shard.MISSING_SHARD);
		
		WLeftRightPanel root = new WLeftRightPanel(shardSelector, shardPanel);
		this.setRootPanel(root);
		shardSelector.setInsets(Insets.ROOT_PANEL);
		
		int i = 0;
		for(Identifier setId : library.getShardSources()) {
			WShardSetPanel panel = new WShardSetPanel();
			panel.setShardConsumer(shardPanel::setShard);
			shardSelector.add(panel, 0, i * 22, 150, 18);
			panel.setShardSet(setId, library, collection);
			
			i++;
			if (i > 4) break;
		}
		
		System.out.println("Added " + i + " shard sets");
		
		root.validate(this);
	}
	
	@Override
	public void addPainters() {
		shardSelector.setBackgroundPainter(BackgroundPainter.createColorful(0xFF_778888));
	}
	
	public static class Screen extends CottonClientScreen {
		public Screen(ShardCollectionComponent collection, ShardLibraryComponent library) {
			super(new ShardTabletGuiDescription(collection, library));
		}
	}
}
