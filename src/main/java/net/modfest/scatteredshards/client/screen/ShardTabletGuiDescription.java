package net.modfest.scatteredshards.client.screen;

import io.github.cottonmc.cotton.gui.client.CottonClientScreen;
import io.github.cottonmc.cotton.gui.client.LightweightGuiDescription;
import io.github.cottonmc.cotton.gui.widget.WGridPanel;

public class ShardTabletGuiDescription extends LightweightGuiDescription {
	
	public ShardTabletGuiDescription() {
		WGridPanel root = new WGridPanel();
		setRootPanel(root);
		
		//Stuff
		//root.add(new WShardPanel(), titleColor, darkmodeTitleColor);
		
		root.validate(this);
	}
	
	
	
	public static class Screen extends CottonClientScreen {
		public Screen() {
			super(new ShardTabletGuiDescription());
		}
	}
}
