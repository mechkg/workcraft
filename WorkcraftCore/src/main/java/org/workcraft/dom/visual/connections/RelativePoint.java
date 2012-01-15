package org.workcraft.dom.visual.connections;

import java.awt.geom.Point2D;
import java.io.Serializable;

import org.workcraft.util.Maybe;
import org.workcraft.util.Function;

import static org.workcraft.util.Geometry.*;

/**
 * A connection control point coordinate, in a coordinate system where the first connected component is at (0, 0) and the second one is at (1, 0)
 */
public class RelativePoint implements Serializable {
	private static final long serialVersionUID = 1L;
	public RelativePoint(Point2D.Double point) {
		this.point = point;
	}
	
	public static RelativePoint ONE_THIRD = new RelativePoint(new Point2D.Double(1.0/3.0, 0.0));
	public static RelativePoint TWO_THIRDS = new RelativePoint(new Point2D.Double(2.0/3.0, 0.0));
	
	public final Point2D.Double point;
	public Point2D.Double toSpace(Point2D.Double p1, Point2D.Double p2) {
		return add(p1, complexMultiply(point, subtract(p2, p1)));
	}
	
	public static Maybe<RelativePoint> fromSpace(final Point2D.Double p1, final Point2D.Double p2, final Point2D.Double p) {
		return Maybe.Util.applyFunc(complexInverse(subtract(p2, p1)), new Function<Point2D.Double, RelativePoint>(){
			@Override
			public RelativePoint apply(Point2D.Double inverted) {
				return new RelativePoint(complexMultiply(subtract(p, p1), inverted));
			}
		});
	}
}
