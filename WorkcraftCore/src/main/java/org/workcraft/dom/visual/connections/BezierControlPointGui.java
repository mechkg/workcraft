package org.workcraft.dom.visual.connections;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import org.workcraft.dom.visual.GraphicalContent;

public class BezierControlPointGui {

	public static GraphicalContent controlHandleGraphicalContent(Point2D cp, final Point2D origin) {
		return new GraphicalContent() {

			@Override
			public void draw(Graphics2D g) {
				g.setColor(Color.LIGHT_GRAY);
				g.setStroke(new BasicStroke(0.02f));
				
				Line2D l = new Line2D.Double(0, 0, origin.getX(), origin.getY());
				g.draw(l);
			}
		};
	}
}
