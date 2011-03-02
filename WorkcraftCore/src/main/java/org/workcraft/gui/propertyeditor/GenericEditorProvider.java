package org.workcraft.gui.propertyeditor;

import org.workcraft.util.Action;

public interface GenericEditorProvider<T> {
	GenericCellEditor<T> createEditor(T initialValue, Action accept, Action cancel);
}
