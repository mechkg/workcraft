package org.workcraft.dom.visual;

import java.awt.Graphics2D;

public interface GraphicalContent {
	class Empty implements GraphicalContent {
		@Override
		public void draw(Graphics2D graphics) {
		}
	}

	public final static Empty empty = new Empty();
	
	public void draw(Graphics2D graphics);
}
