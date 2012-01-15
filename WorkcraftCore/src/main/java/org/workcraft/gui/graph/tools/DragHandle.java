package org.workcraft.gui.graph.tools;

import java.awt.geom.Point2D;

public interface DragHandle {
	public void cancel();
	public void commit();
	public void setOffset(Point2D.Double offset);
}
