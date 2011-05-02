package org.workcraft.plugins.cpog;

import java.awt.geom.Point2D;

import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;

public class ControlPoint {

	public ControlPoint(ModifiableExpression<Point2D> position) {
		this.position = position;
	}

	public final ModifiableExpression<Point2D> position;
}
