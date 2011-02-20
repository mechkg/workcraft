package org.workcraft.gui;

import java.awt.Graphics2D;

import org.workcraft.dependencymanager.advanced.core.EvaluationContext;
import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.core.ExpressionBase;
import org.workcraft.dependencymanager.advanced.core.Expressions;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.DrawMan;
import org.workcraft.dom.visual.DrawRequest;
import org.workcraft.dom.visual.DrawableNew;
import org.workcraft.dom.visual.GraphicalContent;
import org.workcraft.dom.visual.VisualModel;
import org.workcraft.exceptions.NotSupportedException;
import org.workcraft.gui.graph.tools.Decoration;
import org.workcraft.gui.graph.tools.Decorator;
import org.workcraft.gui.graph.tools.NodeGraphicalContentProvider;
import org.workcraft.util.Func;

public class DefaultReflectiveNodeDecorator implements Func<Decorator, Expression<? extends GraphicalContent>> {
	
	private final Node root;

	public DefaultReflectiveNodeDecorator(Node root) {
		this.root = root;
	}
	
	@Override
	public Expression<? extends GraphicalContent> eval(final Decorator arg) {
		
		return DrawMan.graphicalContent(root, new NodeGraphicalContentProvider() {
			
			@Override
			public Expression<? extends GraphicalContent> getGraphicalContent(Node node) {
				if(node instanceof DrawableNew) {
					final DrawableNew drawable = (DrawableNew) node; 
					final Expression<? extends Decoration> decoration = arg.getDecoration(node);
					return new ExpressionBase<GraphicalContent>(){

						@Override
						protected GraphicalContent evaluate(final EvaluationContext context) {
							return new GraphicalContent() {
								
								@Override
								public void draw(final Graphics2D graphics) {
									context.resolve(drawable.graphicalContent()).draw(new DrawRequest(){
										@Override
										public Decoration getDecoration() {
											return context.resolve(decoration);
										}
										@Override
										public Graphics2D getGraphics() {
											return graphics;
										}
										@Override
										public VisualModel getModel() {
											throw new NotSupportedException();
										}
									});
								}
							};
						}
					
					};
				}
				else
					return Expressions.constant(GraphicalContent.empty);
			}
		});
				

				
	}
}
