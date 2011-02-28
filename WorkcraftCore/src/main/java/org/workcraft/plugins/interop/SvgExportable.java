package org.workcraft.plugins.interop;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import org.workcraft.interop.ServiceHandle;

public interface SvgExportable {
	public static ServiceHandle<SvgExportable> SERVICE_HANDLE = ServiceHandle.createNewService(SvgExportable.class, "Allows writing the service provider to SVG image");
	public void draw(Graphics2D graphics);
	public Rectangle2D getBoundingBox();
}
