package org.workcraft.dom.visual;

import org.w3c.dom.Element;
import org.workcraft.dom.Component;
import org.workcraft.dom.Connection;

public abstract class VisualConnection {
	protected Connection refConnection;
	
	public VisualConnection(Connection refConnection) {
		this.refConnection = refConnection;
	}
	
	public abstract void draw();

	public void toXML(Element vconElement) {
		
	}

	public Connection getReferencedConnection() {
		return refConnection;
	}
}
