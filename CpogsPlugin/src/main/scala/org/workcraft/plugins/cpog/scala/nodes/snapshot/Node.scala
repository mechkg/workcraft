package org.workcraft.plugins.cpog.scala.nodes.snapshot

import org.workcraft.plugins.cpog.optimisation.BooleanFormula
import org.workcraft.plugins.cpog.optimisation.expressions.{Variable => BooleanVariable}
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanVisitor
import org.workcraft.plugins.cpog.VariableState
import org.workcraft.plugins.cpog.optimisation.BooleanFormula
import java.awt.geom.Point2D
import org.workcraft.plugins.cpog.LabelPositioning

sealed trait Node

sealed case class VisualProperties(label: String, labelPositioning: LabelPositioning, position: Point2D)

sealed abstract case class Component(visualProperties:VisualProperties) extends Node

sealed case class VisualArc

sealed case class Arc (first : Vertex, second : Vertex, condition: BooleanFormula[Variable], visual : VisualArc) extends Node

case class Vertex(condition: BooleanFormula[Variable], override val visualProperties:VisualProperties) extends Component (visualProperties)

case class Variable(state : VariableState, label : String, override val visualProperties:VisualProperties) extends Component (visualProperties) with Comparable[Variable] {
    override def getLabel : String = label
    override def compareTo(other:Variable) : Int = getLabel.compareTo(other.getLabel)
    override def accept [T] (visitor:BooleanVisitor[Variable, T]) : T = visitor.visit(BooleanVariable.create(this))
  }

case class RhoClause(formula : BooleanFormula[Variable], override val visualProperties : VisualProperties) extends Component (visualProperties)

sealed case class CPOG(nodes : List[Node])
