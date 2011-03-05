package org.workcraft.dom.visual.connections;

import java.awt.geom.Point2D;
import java.util.List;

import org.workcraft.dependencymanager.advanced.core.Expression;


public interface PolylineConfiguration {
	Expression<? extends List<? extends Point2D>> controlPoints();
}
