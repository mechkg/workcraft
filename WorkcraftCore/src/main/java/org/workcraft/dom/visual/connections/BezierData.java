package org.workcraft.dom.visual.connections;

import java.awt.geom.Point2D;

import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;

public interface BezierData {
	ModifiableExpression<Point2D> cp1();
	ModifiableExpression<Point2D> cp2();
}