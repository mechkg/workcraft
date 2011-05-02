package org.workcraft.dom.visual.connections;

public interface StaticConnectionDataVisitor<T> {
	T visitPolyline(StaticPolylineData data);
	T visitBezier(StaticBezierData data);
}
