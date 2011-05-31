package org.workcraft.plugins.cpog.scala.nodes

import org.workcraft.plugins.cpog.optimisation.BooleanFormula
import org.workcraft.plugins.cpog.optimisation.expressions.{Variable => BooleanVariable}
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanVisitor
import org.workcraft.plugins.cpog.VariableState
import org.workcraft.plugins.cpog.optimisation.BooleanFormula
import java.awt.geom.Point2D
import org.workcraft.plugins.cpog.LabelPositioning
import java.util.UUID

package snapshot {

	sealed case class Id[Entity] (id : UUID) {
	}
	
	sealed trait Node
	
	sealed case class VisualProperties(label : String, labelPositioning: LabelPositioning, position: Point2D)
	
	sealed abstract case class Component(val visualProperties:VisualProperties) extends Node
	
	sealed abstract class VisualArc
	
	object VisualArc {
		sealed case class Bezier(cp1 : Point2D, cp2 : Point2D) extends VisualArc
		sealed case class Polyline(cps : List[Point2D]) extends VisualArc
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
