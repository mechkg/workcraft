package org.workcraft.gui.propertyeditor;

import org.workcraft.util.Action;

public interface EditorProvider {
	public SimpleCellEditor getEditor(Action close);
}
