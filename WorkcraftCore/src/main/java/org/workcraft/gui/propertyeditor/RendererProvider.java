package org.workcraft.gui.propertyeditor;

import java.awt.Component;

public interface RendererProvider<T> {
	public Component createRenderer(T value);
}
