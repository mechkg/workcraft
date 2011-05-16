import org.workcraft.plugins.cpog.scala.VisualProperties

import org.workcraft.dependencymanager.advanced.core.ExpressionBase
import org.workcraft.dependencymanager.advanced.user.StorageManager
import org.workcraft.plugins.cpog.optimisation.expressions.One
import org.workcraft.plugins.cpog.optimisation.BooleanFormula
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression
import org.workcraft.plugins.cpog.optimisation.BooleanVariable
import org.workcraft.dependencymanager.advanced.core.GlobalCache._
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanVisitor
import org.workcraft.plugins.cpog.VariableState
import java.awt.geom.Point2D
import org.workcraft.plugins.cpog.optimisation.expressions.Zero
import org.workcraft.plugins.cpog.LabelPositioning
import org.workcraft.dependencymanager.advanced.core.Expression
import org.workcraft.dependencymanager.advanced.core.EvaluationContext
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.BooleanReplacer
import java.util.HashMap
import org.workcraft.plugins.cpog.scala.VisualArc
import org.workcraft.plugins.cpog.scala.VisualArc.Bezier

package org.workcraft.plugins.cpog.scala.nodes {

  sealed abstract class Node {
    final def accept[A](visitor : org.workcraft.plugins.cpog.NodeVisitor[A]) = this match {
      case a : Arc => visitor.visitArc(a)
      case c : Component => visitor.visitComponent(c)
    }
  }
  
  sealed abstract case class Component(visualProperties:VisualProperties) extends Node {
    final def accept[A](visitor : org.workcraft.plugins.cpog.ComponentVisitor[A]) = this match {
      case v : Vertex => visitor.visitVertex(v)
      case v : Variable => visitor.visitVariable(v)
      case r : RhoClause => visitor.visitRho(r)
    }
  }
  
  case class Arc (first : Vertex, second : Vertex, condition: ModifiableExpression[BooleanFormula[Variable]], visual : ModifiableExpression[VisualArc]) extends Node
  
  case class Vertex(condition: ModifiableExpression[BooleanFormula[Variable]], override val visualProperties:VisualProperties) extends Component (visualProperties)
   
  case class Variable(state:ModifiableExpression[VariableState], label: ModifiableExpression[String], override val visualProperties:VisualProperties) 
  			extends Component (visualProperties) with Comparable[Variable] {
  
    override def getLabel : String = eval(label)
  
    override def compareTo(other:Variable) : Int = getLabel.compareTo(other.getLabel)
    
    override def accept [T] (visitor:BooleanVisitor[T]) : T = visitor.visit(this)
  }
  
  case class RhoClause(formula:ModifiableExpression[BooleanFormula[Variable]], override val visualProperties:VisualProperties) extends Component (visualProperties)
  
  object Arc {
    def create (storage: StorageManager, first : Vertex, second : Vertex) = {
      Arc (first, second, storage.create (One.instance), 
          storage.create (Bezier(storage.create(new Point2D.Double(1/3.0,0)), storage.create(new Point2D.Double(2/3.0,0)))))    
    }
  }
  
  object Variable {
    def create(storage:StorageManager, varName:ModifiableExpression[String]) : Variable = 
      Variable (
          storage.create(VariableState.UNDEFINED),
          varName,
          VisualProperties(varName, storage.create(LabelPositioning.BOTTOM), storage.create(new Point2D.Double(0,0)))
          )
  }
  
  object Vertex {
    def create (storage:StorageManager) = Vertex(storage.create (One.instance), VisualProperties.create(storage))
  }
  
  object RhoClause {
    def create (storage:StorageManager) = RhoClause (storage.create(One.instance), VisualProperties.create(storage))
  }
}
