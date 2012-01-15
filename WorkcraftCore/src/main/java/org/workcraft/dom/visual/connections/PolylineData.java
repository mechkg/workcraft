package org.workcraft.dom.visual.connections;

import java.awt.geom.Point2D;
import java.util.Collection;

import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;

public interface PolylineData {
	Collection<ModifiableExpression<Point2D.Double>> controlPoints();
}
