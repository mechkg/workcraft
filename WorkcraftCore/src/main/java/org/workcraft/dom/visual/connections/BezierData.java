package org.workcraft.dom.visual.connections;

import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;

public interface BezierData {
	ModifiableExpression<RelativePoint> cp1();
	ModifiableExpression<RelativePoint> cp2();
}
