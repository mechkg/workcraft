package org.workcraft.gui.graph.tools;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.Expressions;
import org.workcraft.dom.Node;

public interface Colorisator {
	static Colorisator EMPTY = new Colorisator(){
		@Override
		public Expression<? extends Colorisation> getColorisation(Node node) {
			return Expressions.constant(Colorisation.EMPTY);
		}
	};

	Expression<? extends Colorisation> getColorisation(Node node);
}
