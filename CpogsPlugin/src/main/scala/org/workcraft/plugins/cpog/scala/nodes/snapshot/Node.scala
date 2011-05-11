package org.workcraft.plugins.cpog.scala.nodes.snapshot

import org.workcraft.plugins.cpog.optimisation.GenericBooleanFormula
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanVisitor
import org.workcraft.plugins.cpog.VariableState
import org.workcraft.plugins.cpog.optimisation.BooleanVariable
import org.workcraft.plugins.cpog.optimisation.BooleanFormula
import java.awt.geom.Point2D
import org.workcraft.plugins.cpog.LabelPositioning

sealed trait Node

sealed case class VisualProperties(label: String, labelPositioning: LabelPositioning, position: Point2D)

sealed abstract case class Component(visualProperties:VisualProperties) extends Node

sealed case class VisualArc

sealed case class Arc (first : Vertex, second : Vertex, condition: BooleanFormula, visual : VisualArc) extends Node

case class Vertex(condition: GenericBooleanFormula[Variable], override val visualProperties:VisualProperties) extends Component (visualProperties)

case class Variable(state : VariableState, label : String, override val visualProperties:VisualProperties) extends Component (visualProperties) with BooleanVariable with Comparable[Variable] {
    override def getLabel : String = label
    override def compareTo(other:Variable) : Int = getLabel.compareTo(other.getLabel)
    override def accept [T] (visitor:BooleanVisitor[T]) : T = visitor.visit(this)
  }

case class RhoClause(formula : BooleanFormula, override val visualProperties : VisualProperties) extends Component (visualProperties)

sealed case class CPOG(nodes : List[Node])
