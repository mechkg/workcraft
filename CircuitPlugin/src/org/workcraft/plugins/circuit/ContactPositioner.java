package org.workcraft.plugins.circuit;

import java.awt.geom.Point2D;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;

public interface ContactPositioner {
	Point2D position(VisualContact contact, Point2D wantedPosition, EvaluationContext context);
}
