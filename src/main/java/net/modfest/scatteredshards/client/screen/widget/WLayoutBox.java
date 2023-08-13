package net.modfest.scatteredshards.client.screen.widget;

import io.github.cottonmc.cotton.gui.widget.WBox;
import io.github.cottonmc.cotton.gui.widget.WWidget;
import io.github.cottonmc.cotton.gui.widget.data.Axis;

public class WLayoutBox extends WBox {

	public WLayoutBox(Axis axis) {
		super(axis);
	}
	
	@Override
	public void add(WWidget widget) {
		widget.setParent(this);
		children.add(widget);
		
		if (widget.canResize()) {
			int offAxisSize = axis.choose(this.getHeight() - insets.top() - insets.bottom(), this.getWidth() - insets.left() - insets.right());
			
			if (this.axis == Axis.VERTICAL) {
				widget.setSize(offAxisSize, 18);
			} else {
				widget.setSize(18, offAxisSize);
			}
		}
	}
	
	@Override
	public void layout() {
		//Create a layout of children along the box's primary axis
		int numChildren = children.size();
		int[] childOffsets = new int[numChildren];
		int[] childSizes = new int[numChildren];
		
		//Log sizes along this axis
		for(int i=0; i<numChildren; i++) {
			WWidget w = children.get(i);
			childSizes[i] = (axis == Axis.HORIZONTAL) ? w.getWidth() : w.getHeight();
		}
		
		//Log offsets along this axis
		int curOffset = axis.choose(insets.left(), insets.top());
		for(int i=0; i<numChildren; i++) {
			childOffsets[i] = curOffset;
			curOffset += childSizes[i];
			curOffset += this.spacing;
		}
		
		//TODO: Expand to fit
		
		//TODO: Shrink to fit
		
		//Actually lay out children
		int offAxisSize = axis.choose(this.getHeight() - insets.top() - insets.bottom(), this.getWidth() - insets.left() - insets.right());
		for(int i=0; i<numChildren; i++) {
			WWidget cur = children.get(i);
			if (axis == Axis.HORIZONTAL) {
				int yofs = switch (verticalAlignment) {
				case TOP -> 0;
				case BOTTOM -> offAxisSize - cur.getHeight();
				case CENTER -> (offAxisSize / 2) - (cur.getHeight() / 2);
				};
				
				cur.setSize(childSizes[i], cur.getHeight());
				cur.setLocation(childOffsets[i], insets.top() + yofs);
				
			} else {
				int xofs = switch (horizontalAlignment) {
				case LEFT -> 0;
				case RIGHT -> offAxisSize - cur.getWidth();
				case CENTER -> (offAxisSize / 2) - (cur.getWidth() / 2);
				};
				
				cur.setSize(cur.getWidth(), childSizes[i]);
				cur.setLocation(insets.left() + xofs, childOffsets[i]);
			}
		}
	}
}
