package org.workcraft.dom.visual.connections;

import java.awt.geom.Point2D;
import java.util.List;

import org.workcraft.dependencymanager.advanced.core.Expression;

public interface ConnectionGraphicConfigurationVisitor<T> {
	public T visitPolyline(Expression<? extends List<? extends Point2D>> controlPoints);
	public T visitBezier(Expression<? extends Point2D> controlPoint1, Expression<? extends Point2D> controlPoint2);
}
