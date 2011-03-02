package org.workcraft.gui.propertyeditor;

import java.awt.Component;

public interface GenericCellEditor<T> {
	Component component();
	T getValue();
}
