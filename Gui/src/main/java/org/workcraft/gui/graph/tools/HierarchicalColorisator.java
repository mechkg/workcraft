package org.workcraft.gui.graph.tools;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dom.Node;

public abstract class HierarchicalColorisator implements Colorisator {

	private final Colorisation defaultColorisation;

	public HierarchicalColorisator() {
		this(Colorisation.EMPTY);
	}
	public HierarchicalColorisator(Colorisation defaultColorisation) {
		this.defaultColorisation = defaultColorisation;
	}
	
	/**
	 * Returns decoration to apply to this node and its children.
	 * Overrides decoration applied by parents unless the returned decoration is null.
	 * @param node
	 * The node to be decorated
	 * @return
	 * Decoration to be applied
	 */
	public abstract Expression<Colorisation> getSimpleColorisation(Node node);
	
	@Override
	public Expression<Colorisation> getColorisation(final Node node) {
		return new ExpressionBase<Colorisation> () {

			@Override
			protected Colorisation evaluate(EvaluationContext context) {
				if(node == null)
					return defaultColorisation;
				final Colorisation simpleColorisation = context.resolve(getSimpleColorisation(node));
				if(simpleColorisation != null)
					return simpleColorisation;
				else
					return context.resolve(getColorisation(context.resolve(node.parent())));
			}
			
		};
	}

}
