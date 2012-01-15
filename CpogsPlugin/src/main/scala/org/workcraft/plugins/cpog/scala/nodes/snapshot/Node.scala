package org.workcraft.plugins.cpog.scala.nodes

import org.workcraft.plugins.cpog.optimisation.BooleanFormula
import org.workcraft.plugins.cpog.optimisation.expressions.{Variable => BooleanVariable}
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanVisitor
import org.workcraft.plugins.cpog.VariableState
import org.workcraft.plugins.cpog.optimisation.BooleanFormula
import java.awt.geom.Point2D
import org.workcraft.graphics.LabelPositioning
import java.util.UUID
import org.workcraft.dom.visual.connections.VisualConnectionData
import org.workcraft.dom.visual.connections.StaticConnectionDataVisitor
import org.workcraft.dom.visual.connections.StaticBezierData
import org.workcraft.dom.visual.connections.StaticPolylineData
import scala.collection.JavaConversions.asJavaList
import org.workcraft.dom.visual.connections.StaticVisualConnectionData
import org.workcraft.dom.visual.connections.RelativePoint

package snapshot {

	sealed case class Id[Entity] (id : UUID)
	
	sealed trait Node
	
	sealed case class VisualProperties(label : String, labelPositioning: LabelPositioning, position: Point2D.Double)
	
	sealed abstract case class Component(val visualProperties:VisualProperties) extends Node
	
	sealed abstract class VisualArc extends StaticVisualConnectionData
	
	object VisualArc {
		sealed case class Bezier(override val cp1 : RelativePoint, override val cp2 : RelativePoint) extends VisualArc with StaticBezierData {
		  override def accept[T](visitor : StaticConnectionDataVisitor[T]) = {
      		visitor.visitBezier(this)
		  }
		}
		sealed case class Polyline(cps : List[Point2D.Double]) extends VisualArc with StaticPolylineData {
		  override def controlPoints = asJavaList(cps)
		  override def accept[T](visitor : StaticConnectionDataVisitor[T]) = {
      		visitor.visitPolyline(this)
		  }
		}
	}
	
	sealed case class Arc (first : Id[Vertex], second : Id[Vertex], condition: BooleanFormula[Id[Variable]], visual : VisualArc) extends Node
	
	case class Vertex(condition: BooleanFormula[Id[Variable]], override val visualProperties:VisualProperties) extends Component (visualProperties)
	
	case class Variable(state : VariableState, override val visualProperties:VisualProperties) extends Component (visualProperties)
	
	case class RhoClause(formula : BooleanFormula[Id[Variable]], override val visualProperties : VisualProperties) extends Component (visualProperties)
	
	object CPOG {
		type Storage[A]=Map[Id[A],A]
	}
	import CPOG._
	
	sealed case class CPOG(variables : Storage[Variable], vertices : Storage[Vertex], arcs : List[Arc], rhoClauses : List[RhoClause])
}
