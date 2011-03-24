package org.workcraft.dom.visual.connections;


public interface ConnectionGraphicConfigurationVisitor<T> {
	public T visitPolyline(Polyline polyline);
	public T visitBezier(Bezier bezier);
}
