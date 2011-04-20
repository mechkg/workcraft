package org.workcraft.plugins.cpog.scala

import org.workcraft.plugins.cpog.Vertex
import org.workcraft.plugins.cpog.Variable
import org.workcraft.plugins.cpog.RhoClause
import java.awt.geom.Point2D
import org.workcraft.plugins.cpog.Component
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression


class MovableController {
	
  def componentMovableController (component: Component) : ModifiableExpression[Point2D] = {
	  component match {
	    case rho:RhoClause => rho.visualInfo.position
	    case v : Variable  => v.visualVar.position
	    case v : Vertex    => v.visualInfo.position
	  }
	}
  
    
  
  
  /*
  
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
*/
 
}