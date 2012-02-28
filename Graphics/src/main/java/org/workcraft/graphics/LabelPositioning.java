package org.workcraft.graphics;

import static pcollections.TreePVector.empty;

import java.awt.geom.AffineTransform;
import java.awt.geom.Rectangle2D;

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
	
	public static AffineTransform positionRelative(Rectangle2D what, Rectangle2D relativeTo, LabelPositioning positioning) {
		double tx = -what.getCenterX() + relativeTo.getCenterX() + 0.5 * positioning.dx * (relativeTo.getWidth() + what.getWidth() + 0.2);
		double ty = -what.getCenterY() + relativeTo.getCenterY() + 0.5 * positioning.dy * (relativeTo.getHeight() + what.getHeight() + 0.2);
		
		return AffineTransform.getTranslateInstance(tx, ty);
	}
}
