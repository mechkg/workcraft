package org.workcraft.gui.graph.tools;

import java.awt.geom.Point2D;

import pcollections.PSet;

public interface HitTester<Node> {
	Node hitTest(Point2D point);
	PSet<Node> boxHitTest(Point2D boxStart, Point2D boxEnd);
}