package org.workcraft.gui.graph.tools;

import java.awt.geom.Point2D;

import pcollections.PCollection;

public interface HitTester<Node> {
	Node hitTest(Point2D point);
	PCollection<Node> boxHitTest(Point2D boxStart, Point2D boxEnd);
}
