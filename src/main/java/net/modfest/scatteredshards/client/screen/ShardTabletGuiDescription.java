package net.modfest.scatteredshards.client.screen;

import java.util.List;

import io.github.cottonmc.cotton.gui.client.BackgroundPainter;
import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WListPanel;
import io.github.cottonmc.cotton.gui.widget.WPanelWithInsets;
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
	WPlainPanel selectorPanel = new WPlainPanel();
	WScrollBar shardSelectorScrollBar = new WScrollBar(Axis.VERTICAL);
	WListPanel<Identifier, WShardSetPanel> shardSelector;
	
	public ShardTabletGuiDescription(ShardCollectionComponent collection, ShardLibraryComponent library) {
		this.collection = collection;
		this.library = library;
		
		shardPanel.setShard(Shard.MISSING_SHARD);
		shardPanel.setHidden(true);
		
		shardSelector = new WListPanel<Identifier, WShardSetPanel>(List.copyOf(this.library.getShardSources()), WShardSetPanel::new, this::configurePanel);
		selectorPanel.setInsets(Insets.ROOT_PANEL);
		
		WLeftRightPanel root = new WLeftRightPanel(selectorPanel, shardPanel);
		selectorPanel.add(shardSelector, 0, 0, getLayoutWidth(selectorPanel), getLayoutHeight(selectorPanel));
		
		
		this.setRootPanel(root);
		
		root.validate(this);
	}
	
	private int getLayoutWidth(WPanelWithInsets panel) {
		return panel.getWidth() - panel.getInsets().left() - panel.getInsets().right();
	}
	
	private int getLayoutHeight(WPanelWithInsets panel) {
		return panel.getHeight() - panel.getInsets().top() - panel.getInsets().bottom();
	}
	
	private void configurePanel(Identifier setId, WShardSetPanel panel) {
		panel.setSize(shardSelector.getWidth() - shardSelector.getScrollBar().getWidth(), 20);
		panel.setShardConsumer(shardPanel::setShard);
		panel.setShardSet(setId, library, collection);
	}
	
	@Override
	public void addPainters() {
		selectorPanel.setBackgroundPainter(BackgroundPainter.createColorful(0xFF_778888));
	}
	
	public static class Screen extends CottonClientScreen {
		public Screen(ShardCollectionComponent collection, ShardLibraryComponent library) {
			super(new ShardTabletGuiDescription(collection, library));
		}
	}
}
