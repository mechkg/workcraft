package org.workcraft.plugins.cpog;

import org.workcraft.gui.propertyeditor.EditableProperty;
import org.workcraft.gui.propertyeditor.PropertyClassProvider;
import org.workcraft.gui.propertyeditor.cpog.EncodingProperty;

public class EncodingPropertyProvider implements PropertyClassProvider {

	@Override
	public Class<?> getPropertyType() {
		return Encoding.class;
	}

	@Override
	public EditableProperty getPropertyGui() {
		return new EncodingProperty();
	}

}
