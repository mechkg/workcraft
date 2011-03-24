package org.workcraft.gui.graph.tools.selection;

import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.List;

import org.workcraft.dependencymanager.advanced.core.Expression;
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dom.Node;
import org.workcraft.dom.visual.GraphicalContent;
import org.workcraft.dom.visual.TouchableProvider;
import org.workcraft.dom.visual.VisualScene;
import org.workcraft.dom.visual.VisualTransformableNode;
import org.workcraft.dom.visual.connections.Bezier;
import org.workcraft.dom.visual.connections.ConnectionGraphicConfiguration;
import org.workcraft.dom.visual.connections.ConnectionGraphicConfigurationVisitor;
import org.workcraft.dom.visual.connections.ControlPoint;
import org.workcraft.dom.visual.connections.Polyline;
import org.workcraft.dom.visual.connections.VisualConnection;
import org.workcraft.exceptions.NotImplementedException;
import org.workcraft.gui.graph.tools.MovableController;
import org.workcraft.util.Function;

import pcollections.ConsPStack;
import pcollections.PSequence;
import pcollections.PStack;
import pcollections.PVector;

import static org.workcraft.dependencymanager.advanced.core.Expressions.*;

public class ControlPointsGui {
	private final Expression<PVector<VisualConnection>> selectedConnections;

	public ControlPointsGui(Expression<PVector<VisualConnection>> selectedConnections) {
		this.selectedConnections = selectedConnections;
	}
	
	public static VisualScene<Node> getControlPointsScene(PSequence<Node> selection) {
		VisualScene<Node> result = VisualScene.Util.empty();
		for(Node node : selection)
			if(node instanceof VisualConnection) {
				result = VisualScene.Util.combine(result, getControlPointsScene((VisualConnection) node));
			}
	}

	private static Expression<VisualScene<Node>> getControlPointsScene(VisualConnection node) {
		Expression<VisualScene<Node>> result = bindFunc(node.graphic(), new Function<ConnectionGraphicConfiguration, VisualScene<Node>>(){

			@Override
			public VisualScene<Node> apply(ConnectionGraphicConfiguration argument) {
				return new VisualScene<Node>(){
					
				};
			}
		});		
	}

	Expression<? extends Collection<? extends VisualTransformableNode>> connectionToControlPoints (TouchableProvider<Node> tp, VisualConnection vc) {
		return bind(vc.graphic(), new Function<ConnectionGraphicConfiguration, Expression<? extends Collection<? extends VisualTransformableNode>>>(){

			@Override
			public Expression<? extends Collection<? extends VisualTransformableNode>> apply(ConnectionGraphicConfiguration argument) {
				return argument.accept(new ConnectionGraphicConfigurationVisitor<Expression<? extends Collection<? extends VisualTransformableNode>>>() {

					@Override
					public Expression<? extends Collection<? extends VisualTransformableNode>> visitPolyline(Polyline polyline) {
						return polyline.controlPoints();
					}

					@Override
					public Expression<? extends Collection<? extends VisualTransformableNode>> visitBezier(Bezier bezier) {
						return bezier.children();
					}
				});
			}
		});
	};
	
	Function<PVector<VisualConnection>, GraphicalContent> controlPointsGraphicalContentFunc = new Function<PVector<VisualConnection>, GraphicalContent>() {
		@Override
		public GraphicalContent apply(PVector<VisualConnection> connections) {
			for(VisualConnection conn : connections) {
				conn.graphic().
			}
		}
	};
}
