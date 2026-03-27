package net.modfest.scatteredshards.client.screen;

import io.github.cottonmc.cotton.gui.client.BackgroundPainter;
import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WListPanel;
import io.github.cottonmc.cotton.gui.widget.WPanelWithInsets;
import io.github.cottonmc.cotton.gui.widget.WPlainPanel;
import io.github.cottonmc.cotton.gui.widget.data.HorizontalAlignment;
import io.github.cottonmc.cotton.gui.widget.data.Insets;
import io.github.cottonmc.cotton.gui.widget.data.Vec2i;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.resources.Identifier;
import net.modfest.scatteredshards.api.ScatteredShardsAPI;
import net.modfest.scatteredshards.api.ShardCollection;
import net.modfest.scatteredshards.api.ShardLibrary;
import net.modfest.scatteredshards.api.shard.Shard;
import net.modfest.scatteredshards.api.shard.ShardType;
import net.modfest.scatteredshards.client.screen.widget.WLeftRightPanel;
import net.modfest.scatteredshards.client.screen.widget.WShardPanel;
import net.modfest.scatteredshards.client.screen.widget.WShardSetPanel;
import net.modfest.scatteredshards.client.screen.widget.scalable.WScaledLabel;
import net.modfest.scatteredshards.networking.C2SRequestGlobalCollection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

public class ShardTabletGuiDescription extends LightweightGuiDescription {
	public static int INITIAL_SCROLL_POSITION = 0;
	public static Identifier INITIAL_SHARD = ShardType.MISSING_ID;

	protected final ShardCollection collection;
	protected final ShardLibrary library;

	WShardPanel shardPanel = new WShardPanel();
	WPlainPanel selectorPanel = new WPlainPanel();
	WListPanel<Identifier, WShardSetPanel> shardSelector;

	public ShardTabletGuiDescription(ShardCollection collection, ShardLibrary library) {
		this.collection = collection;
		this.library = library;

		shardPanel.setShard(library.shards().get(INITIAL_SHARD).orElse(Shard.MISSING_SHARD));

		List<Identifier> ids = new ArrayList<>(this.library.shardSets().keySet());
		ids.sort(Comparator.comparing((setId) -> Shard.getSourceForSourceId(setId).getString().toLowerCase()));

		shardSelector = new WListPanel<>(ids, WShardSetPanel::new, this::configurePanel);
		shardSelector.getScrollBar().setScrollingSpeed(1);
		selectorPanel.setInsets(Insets.ROOT_PANEL);

		WLeftRightPanel root = new WLeftRightPanel(selectorPanel, shardPanel);
		selectorPanel.add(shardSelector, 0, 0, getLayoutWidth(selectorPanel), getLayoutHeight(selectorPanel));

		int panelHeight = selectorPanel.getHeight();

		WScaledLabel progressVisited = new WScaledLabel(() -> {
			int visitedSets = 0;
			for (Collection<Identifier> set : library.shardSets().asMap().values()) {
				for (Identifier identifier : set) {
					if (collection.contains(identifier)) {
						visitedSets++;
						break;
					}
				}
			}
			return Component.translatable("gui.scattered_shards.tablet.label.progress.started", "%d/%d".formatted(visitedSets, library.shardSets().asMap().size()));
		}, 1.0f).setColor(CommonColors.LIGHT_GRAY);
		selectorPanel.add(progressVisited, 0, 0);
		progressVisited.setSize(100, 10);
		progressVisited.setLocation(selectorPanel.getInsets().left(), panelHeight - selectorPanel.getInsets().bottom() - 10);
		progressVisited.setHorizontalAlignment(HorizontalAlignment.LEFT);

		WScaledLabel progressTotal = new WScaledLabel(() -> Component.translatable("gui.scattered_shards.tablet.label.progress.total", "%d/%d".formatted(collection.size(), library.shards().size())), 1.0f).setColor(CommonColors.LIGHT_GRAY);
		selectorPanel.add(progressTotal, 0, 0);
		progressTotal.setHorizontalAlignment(HorizontalAlignment.RIGHT);
		progressTotal.setSize(100, 10);
		progressTotal.setLocation(selectorPanel.getWidth() - selectorPanel.getInsets().right() - 100 - 10 /* scrollbar */, panelHeight - selectorPanel.getInsets().bottom() - 10);

		setTitleColor(0xFF_FFFFFF);
		setTitlePos(new Vec2i(root.getX() + root.getWidth() / 2, root.getAbsoluteY() - 10));
		setTitleAlignment(HorizontalAlignment.CENTER);

		ClientPlayNetworking.send(C2SRequestGlobalCollection.INSTANCE);

		this.setRootPanel(root);

		root.validate(this);

		if (shardPanel.getShard() == Shard.MISSING_SHARD && INITIAL_SCROLL_POSITION >= 0) { // Only reload scrolling without a selected shard
			shardSelector.getScrollBar().setValue(INITIAL_SCROLL_POSITION);
		} else if (shardPanel.getShard() != Shard.MISSING_SHARD) { // Try scroll to relevant shard set
			for (int i = 0; i < ids.size(); i++) {
				if (library.shardSets().get(ids.get(i)).contains(INITIAL_SHARD)) {
					shardSelector.getScrollBar().setValue(i - 3); // 7 rows on screen, so center by -3
					break;
				}
			}
		}
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
		selectorPanel.setBackgroundPainter(BackgroundPainter.createColorful(
			0xFF_000000 | (ScatteredShardsAPI.getClientLibrary().shardDisplaySettings().libraryColor())
		));
	}

	public static class Screen extends CottonClientScreen {
		public Screen(ShardCollection collection, ShardLibrary library) {
			super(Component.translatable("menu.scattered_shards.collection"), new ShardTabletGuiDescription(collection, library));
		}

		@Override
		public void onClose() {
			if (description instanceof ShardTabletGuiDescription desc) { // Silly, but description has no onClose.
				INITIAL_SCROLL_POSITION = desc.shardSelector.getScrollBar().getValue();
				INITIAL_SHARD = desc.library.shards().get(desc.shardPanel.getShard()).orElse(ShardType.MISSING_ID);
			}
			super.onClose();
		}
	}
}
