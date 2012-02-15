package org.workcraft.gui.propertyeditor;

import java.awt.Component;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.util.Nothing;

public interface ReactiveComponent {
	/**
	 * @return
	 * The component itself oblivious of any required state changes.
	 */
	public Component component();
	/**
	 * @return
	 * The expression, evaluation of which updates the component state.
	 */
	public Expression<? extends Nothing> updateExpression();
}
