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
import org.workcraft.gui.graph.tools.Colorisation;
import org.workcraft.gui.graph.tools.Colorisator;
import org.workcraft.gui.graph.tools.NodePainter;
import org.workcraft.util.Func;

public class DefaultReflectiveModelPainter {
	
	public static Func<Colorisator, Expression<? extends GraphicalContent>> reflectivePainterProvider(final Node root) {
		return new Func<Colorisator, Expression<? extends GraphicalContent>>(){
			@Override
			public Expression<? extends GraphicalContent> eval(Colorisator colorisator) {
				return createReflectivePainter(root, colorisator);
			}
		};
	}
	
	public static Expression<? extends GraphicalContent> createReflectivePainter(Node root, final Colorisator colorisator) {
		return DrawMan.graphicalContent(root, new NodePainter() {
			@Override
			public Expression<? extends GraphicalContent> getGraphicalContent(Node node) {
				if(node instanceof DrawableNew) {
					final DrawableNew drawable = (DrawableNew) node; 
					final Expression<? extends Colorisation> colorisation = colorisator.getColorisation(node);
					return new ExpressionBase<GraphicalContent>(){

						@Override
						protected GraphicalContent evaluate(final EvaluationContext context) {
							return new GraphicalContent() {
								
								@Override
								public void draw(final Graphics2D graphics) {
									context.resolve(drawable.graphicalContent()).draw(new DrawRequest(){
										@Override
										public Colorisation getColorisation() {
											return context.resolve(colorisation);
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
