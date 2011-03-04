package org.workcraft.dom.visual.connections;

public interface ConnectionGraphicConfigurationVisitor<T> {
	public T visitPolyline(PolylineConfiguration polyline);
	public T visitBezier(BezierConfiguration bezier);
}
