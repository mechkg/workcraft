package org.workcraft.dom.visual;

import java.awt.Graphics2D;

public interface SimpleGraphicalContent {
	class Empty implements SimpleGraphicalContent {

		public static Empty INSTANCE = new Empty();
		
		@Override
		public void draw(Graphics2D graphics) {
		}
		
	}

	public void draw(Graphics2D graphics);
}
