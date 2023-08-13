package net.modfest.scatteredshards.client.screen.widget;

import io.github.cottonmc.cotton.gui.widget.WPanel;
import io.github.cottonmc.cotton.gui.widget.WPlainPanel;
import io.github.cottonmc.cotton.gui.widget.WWidget;

public class WLeftRightPanel extends WPlainPanel {
	protected final WPanel leftPanel;
	protected final WPanel rightPanel;
	
	public WLeftRightPanel(WPanel left, WPanel right) {
		this.leftPanel = left;
		this.rightPanel = right;
		
		super.add(left, 0, 0, 199, 200);
		super.add(right, 198, 0, 124, 200);
	}
	
	@Override
	public void layout() {
		leftPanel.setLocation(0, 0);
		leftPanel.setSize(199, 200);
		rightPanel.setLocation(198, 0);
		rightPanel.setSize(124, 200);
		leftPanel.layout();
		rightPanel.layout();
	}
	
	@Override
	public void add(WWidget w, int x, int y) {
		throw new UnsupportedOperationException("Cannot add widgets to a LeftRightPanel.");
	}
	
	@Override
	public void add(WWidget w, int x, int y, int width, int height) {
		throw new UnsupportedOperationException("Cannot add widgets to a LeftRightPanel.");
	}
}
