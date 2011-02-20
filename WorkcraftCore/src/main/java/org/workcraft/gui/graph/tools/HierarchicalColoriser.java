package org.workcraft.gui.graph.tools;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dom.Node;

public abstract class HierarchicalColoriser implements Decorator {

	/**
	 * Returns decoration to apply to this node and its children.
	 * Overrides decoration applied by parents unless the returned decoration is null.
	 * @param node
	 * The node to be decorated
	 * @return
	 * Decoration to be applied
	 */
	public abstract Expression<Decoration> getElementaryDecoration(Node node);
	
	@Override
	public Expression<Decoration> getDecoration(final Node node) {
		return new ExpressionBase<Decoration> () {

			@Override
			protected Decoration evaluate(EvaluationContext context) {
				if(node == null)
					return Decoration.EMPTY;
				final Decoration elementaryDecoration = context.resolve(getElementaryDecoration(node));
				if(elementaryDecoration != null)
					return elementaryDecoration;
				else
					return context.resolve(getDecoration(context.resolve(node.parent())));
			}
			
		};
	}

}
