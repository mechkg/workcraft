package org.workcraft.dom.visual;

import java.awt.Graphics2D;

public interface GraphicalContent {
	public final static GraphicalContent EMPTY = new GraphicalContent() {
		@Override
		public void draw(Graphics2D graphics) {
		}
	};
	
	public void draw(Graphics2D graphics);
	
	public class Util {
		public static GraphicalContent compose(final GraphicalContent a, final GraphicalContent b) {
			return new GraphicalContent (){
				@Override
				public void draw(Graphics2D graphics) {
					a.draw(graphics);
					b.draw(graphics);
				}
			};
		}
	}
}
