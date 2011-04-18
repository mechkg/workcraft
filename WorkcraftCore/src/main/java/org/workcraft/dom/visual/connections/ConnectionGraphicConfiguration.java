package org.workcraft.dom.visual.connections;


public interface ConnectionGraphicConfiguration {
	public <T> T accept(ConnectionGraphicConfigurationVisitor<T> visitor);
}
