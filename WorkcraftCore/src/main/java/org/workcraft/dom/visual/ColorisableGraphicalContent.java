package org.workcraft.dom.visual;

public interface ColorisableGraphicalContent {
	ColorisableGraphicalContent EMPTY = new ColorisableGraphicalContent(){
		@Override
		public void draw(DrawRequest request) {
		}
	};

	public void draw(DrawRequest request);
}
