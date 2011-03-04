package org.workcraft.dom.visual.connections;

import java.awt.geom.Point2D;

import org.workcraft.dependencymanager.advanced.core.Expression;

public interface BezierConfiguration {
	public Expression<? extends Point2D> controlPoint1();
	public Expression<? extends Point2D> controlPoint2();
}
