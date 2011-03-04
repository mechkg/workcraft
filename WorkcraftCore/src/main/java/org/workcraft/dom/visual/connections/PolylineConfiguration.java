package org.workcraft.dom.visual.connections;

import java.awt.geom.Point2D;

import org.workcraft.dependencymanager.advanced.core.Expression;

import pcollections.PCollection;

public interface PolylineConfiguration {
	Expression<? extends PCollection<Point2D>> controlPoints();
}
