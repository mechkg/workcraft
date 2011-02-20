package org.workcraft.gui.graph.tools;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.Expressions;
import org.workcraft.dom.Node;

public interface Decorator {
	static Decorator EMPTY = new Decorator(){
		@Override
		public Expression<? extends Decoration> getDecoration(Node node) {
			return Expressions.constant(Decoration.EMPTY);
		}
	};

	Expression<? extends Decoration> getDecoration(Node node);
}
