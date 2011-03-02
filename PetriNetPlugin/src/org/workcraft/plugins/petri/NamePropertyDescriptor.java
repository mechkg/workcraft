
package org.workcraft.plugins.petri;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpressionBase;
import org.workcraft.dom.Node;
import org.workcraft.gui.propertyeditor.EditableProperty;
import org.workcraft.gui.propertyeditor.string.StringProperty;

final class NamePropertyDescriptor {

	public static EditableProperty create(final PetriNet petriNet, final Node node) {
		return StringProperty.create("Name", new ModifiableExpressionBase<String>(){

			@Override
			public void setValue(String newValue) {
				petriNet.setName(node, newValue);
			}

			@Override
			protected String evaluate(EvaluationContext context) {
				return petriNet.getName(node);
			}
		});
	}
}