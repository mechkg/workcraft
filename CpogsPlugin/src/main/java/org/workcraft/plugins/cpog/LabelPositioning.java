package org.workcraft.plugins.cpog;

import static org.workcraft.dom.visual.BoundedColorisableGraphicalContent.*;
import static pcollections.TreePVector.*;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.dom.visual.BoundedColorisableGraphicalContent;
import org.workcraft.util.Pair;

import pcollections.PVector;

public enum LabelPositioning {
	TOP("Top", 0, -1),
	LEFT("Left", -1, 0),
	RIGHT("Right", 1, 0),
	BOTTOM("Bottom", 0, 1),
	CENTER("Center", 0, 0);

	public final String name;
	public final int dx, dy;

	private LabelPositioning(String name, int dx, int dy) {
		this.name = name;
		this.dx = dx;
		this.dy = dy;
	}

	public static PVector<Pair<String, LabelPositioning>> getChoice() {
		PVector<Pair<String, LabelPositioning>> positions = empty();
		
		for(LabelPositioning lp : LabelPositioning.values())
			positions = positions.plus(Pair.of(lp.name, lp));
		
		return positions;
	}
	
	public static BoundedColorisableGraphicalContent positionRelative(Rectangle2D bb, LabelPositioning positioning, BoundedColorisableGraphicalContent image) {
		BoundedColorisableGraphicalContent centered = centerToZero.apply(image);
		
		Point2D labelPosition = new Point2D.Double(
				bb.getCenterX() + 0.5 * positioning.dx * (bb.getWidth() + centered.boundingBox.getWidth() + 0.2),
				bb.getCenterY() + 0.5 * positioning.dy * (bb.getHeight() + centered.boundingBox.getHeight() + 0.2));
		
		return translate(centered, labelPosition);
	}
}
