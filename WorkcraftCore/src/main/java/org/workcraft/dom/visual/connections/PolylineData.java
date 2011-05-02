package org.workcraft.dom.visual.connections;

import java.awt.geom.Point2D;
import java.util.List;

import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;

public interface PolylineData {
	List<ModifiableExpression<Point2D>> controlPoints();
}