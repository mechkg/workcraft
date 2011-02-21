package org.workcraft.gui.graph.tools;

import java.awt.Color;

public interface Colorisation {
	Color getColorisation();
	Color getBackground();
	
	public static class Empty implements Colorisation {

		@Override
		public Color getColorisation() {
			return null;
		}

		@Override
		public Color getBackground() {
			return null;
		}
	}
	
	public static final Empty EMPTY = new Empty();
}
