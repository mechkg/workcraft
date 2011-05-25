package org.workcraft.plugins.cpog.scala.nodes

import org.workcraft.plugins.cpog.optimisation.BooleanFormula
import org.workcraft.plugins.cpog.optimisation.expressions.{Variable => BooleanVariable}
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanVisitor
import org.workcraft.plugins.cpog.VariableState
import org.workcraft.plugins.cpog.optimisation.BooleanFormula
import java.awt.geom.Point2D
import org.workcraft.plugins.cpog.LabelPositioning
import java.util.UUID

package object snapshot {

	sealed case class Id[Entity] (id : UUID) {
	}
	
	sealed trait Node
	
	sealed case class VisualProperties(label: String, labelPositioning: LabelPositioning, position: Point2D)
	
	sealed abstract class Component(val visualProperties:VisualProperties) extends Node
	
	sealed case class VisualArc ()
	
	sealed case class Arc (first : Id[Vertex], second : Id[Vertex], condition: BooleanFormula[Id[Variable]], visual : VisualArc) extends Node
	
	case class Vertex(condition: BooleanFormula[Id[Variable]], visualProperties:VisualProperties) extends Component (visualProperties)
	
	case class Variable(state : VariableState, label : String, visualProperties:VisualProperties) extends Component (visualProperties)
	
	case class RhoClause(formula : BooleanFormula[Id[Variable]], visualProperties : VisualProperties) extends Component (visualProperties)
	
	type Storage[A]=Map[Id[A],A]
	
	sealed case class CPOG(variables : Storage[Variable], vertices : Storage[Vertex], arcs : List[Arc], rhoClauses : List[RhoClause])
}
