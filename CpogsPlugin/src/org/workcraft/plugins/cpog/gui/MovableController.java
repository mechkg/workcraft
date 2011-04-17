package org.workcraft.plugins.cpog.gui;

import java.awt.geom.Point2D;

import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.plugins.cpog.Arc;
import org.workcraft.plugins.cpog.Component;
import org.workcraft.plugins.cpog.ComponentVisitor;
import org.workcraft.plugins.cpog.Node;
import org.workcraft.plugins.cpog.NodeVisitor;
import org.workcraft.plugins.cpog.RhoClause;
import org.workcraft.plugins.cpog.Variable;
import org.workcraft.plugins.cpog.Vertex;
import org.workcraft.util.Function;
import org.workcraft.util.Maybe;

import static org.workcraft.util.Maybe.Util.*;

public class MovableController {
	/**
	 *	<pre>
	 * componentMovableController :: Component -> ModifiableExpression Point2D
	 * componentMovableController component = case component of
	 *     (RhoClause rho) -> position (visualInfo rho)
	 *     (Variable v) -> position (visualVar v)
	 *     (Vertex v) -> position (visualInfo v)</pre>
	 */
	public final static Function<Component, ModifiableExpression<Point2D>> componentMovableController = new Function<Component, ModifiableExpression<Point2D>>(){
		@Override
		public ModifiableExpression<Point2D> apply(Component component) {
			return component.accept(new ComponentVisitor<ModifiableExpression<Point2D>>() {
				@Override
				public ModifiableExpression<Point2D> visitRho(RhoClause rho) {
					return rho.visualInfo.position;
				}

				@Override
				public ModifiableExpression<Point2D> visitVariable(Variable variable) {
					return variable.visualVar.position;
				}

				@Override
				public ModifiableExpression<Point2D> visitVertex(Vertex vertex) {
					return vertex.visualInfo.position;
				}
			});
		}
	};
	
	/**
     * <pre>
     * movableController :: Node -> Maybe (ModifiableExpression Point2D)
	 * movableController node = case node of
	 *     Arc _ -> Nothing
	 *     Component c -> Just $ componentMovableController c
	 *</pre>
	 */

	public final static org.workcraft.gui.graph.tools.MovableController<Node> movableController = new org.workcraft.gui.graph.tools.MovableController<Node>(){
		@Override
		public Maybe<? extends ModifiableExpression<Point2D>> apply(Node node) {
			return node.accept(new NodeVisitor<Maybe<? extends ModifiableExpression<Point2D>>>() {
				@Override
				public Maybe<? extends ModifiableExpression<Point2D>> visitArc(Arc arc) {
					return nothing();
				}

				@Override
				public Maybe<? extends ModifiableExpression<Point2D>> visitComponent(Component component) {
					return just(componentMovableController.apply(component));
				}
			});
		}
	};
}
