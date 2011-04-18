package org.workcraft.dom.visual.connections;

public interface ConnectionDataVisitor<T> {
	T visitPolyline(PolylineData data);
	T visitBezier(BezierData data);
}