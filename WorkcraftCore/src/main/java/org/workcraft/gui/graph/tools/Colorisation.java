package org.workcraft.gui.graph.tools;

import java.awt.Color;

public interface Colorisation {
	Color getColorisation();
	Color getBackground();
	
	public static final Colorisation EMPTY = new Colorisation() {
		@Override
		public Color getColorisation() {
			return null;
		}
		@Override
		public Color getBackground() {
			return null;
		}
	};
}
