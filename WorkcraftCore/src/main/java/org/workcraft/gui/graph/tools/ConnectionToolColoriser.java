package org.workcraft.gui.graph.tools;

import java.awt.Color;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dom.Node;

public class ConnectionToolColoriser {
	private static Color highlightColor = new Color(99, 130, 191).brighter();
	public Colorisator getColorisator(final Expression<Node> mouseOverObject) {
		return new HierarchicalColorisator() {

			@Override
			public Expression<Colorisation> getSimpleColorisation(final Node node) {
				return new ExpressionBase<Colorisation>(){
					@Override
					protected Colorisation evaluate(EvaluationContext context) {
						if(node == context.resolve(mouseOverObject))
							return new Colorisation(){
								
								@Override
								public Color getColorisation() {
									return highlightColor;
								}
		
								@Override
								public Color getBackground() {
									return null;
								}
						};
						return null;
					}
				};
			};
		
		};
	}
}
