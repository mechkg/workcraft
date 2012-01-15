package org.workcraft.plugins.cpog.gui;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.dom.visual.Touchable;
import org.workcraft.util.Function;

public class TouchableProvider {

	public static Function<Rectangle2D.Double, Touchable> bbToTouchable = new Function<Rectangle2D.Double, Touchable>() {
		@Override
		public Touchable apply(final Rectangle2D.Double bb) {
			return new Touchable(){

				@Override
				public boolean hitTest(Point2D.Double point) {
					return getBoundingBox().contains(point);
				}

				@Override
				public Rectangle2D.Double getBoundingBox() {
					return bb;
				}

				@Override
				public Point2D.Double getCenter() {
					return new Point2D.Double(0, 0);
				}
			};
		}
	};
}
