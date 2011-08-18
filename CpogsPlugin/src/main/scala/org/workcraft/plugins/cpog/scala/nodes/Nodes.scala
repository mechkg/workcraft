
import org.workcraft.scala.Expressions._
import org.workcraft.scala.StorageManager
import org.workcraft.plugins.cpog.optimisation.expressions.One
import org.workcraft.plugins.cpog.optimisation.BooleanFormula
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanVisitor
import org.workcraft.plugins.cpog.VariableState
import java.awt.geom.Point2D
import org.workcraft.plugins.cpog.optimisation.expressions.Zero
import org.workcraft.graphics.LabelPositioning
import java.util.HashMap
import org.workcraft.plugins.cpog.scala.VisualArc
import org.workcraft.plugins.cpog.scala.VisualArc.Bezier
import org.workcraft.dom.visual.connections.RelativePoint

package org.workcraft.plugins.cpog.scala.nodes {

  sealed abstract class Node {
    final def accept[A](visitor : org.workcraft.plugins.cpog.NodeVisitor[A]) = this match {
      case a : Arc => visitor.visitArc(a)
      case c : Component => visitor.visitComponent(c)
    }
  }
  sealed abstract case class Component(val visualProperties:VisualProperties) extends Node {
    final def accept[A](visitor : org.workcraft.plugins.cpog.ComponentVisitor[A]) = this match {
      case v : Vertex => visitor.visitVertex(v)
      case v : Variable => visitor.visitVariable(v)
      case r : RhoClause => visitor.visitRho(r)
    }
  }
   
  case class Arc (first : Vertex, second : Vertex, condition: ModifiableExpression[BooleanFormula[Variable]], visual : ModifiableExpression[VisualArc]) extends Node
  
  case class Vertex(condition: ModifiableExpression[BooleanFormula[Variable]], override val visualProperties:VisualProperties) extends Component (visualProperties)
   
  case class Variable(state:ModifiableExpression[VariableState], override val visualProperties:VisualProperties) 
  			extends Component (visualProperties) with Comparable[Variable] {
  
    private def getLabel : String = eval(visualProperties.label)
  
    override def compareTo(other:Variable) : Int = getLabel.compareTo(other.getLabel)
  }
  
  case class RhoClause(formula:ModifiableExpression[BooleanFormula[Variable]], override val visualProperties:VisualProperties) extends Component (visualProperties)
  
  object Arc {
    def create (storage: StorageManager, first : Vertex, second : Vertex) = {
      Arc (first, second, storage.create (One.instance[Variable]), 
          storage.create (Bezier(storage.create(RelativePoint.ONE_THIRD), storage.create(RelativePoint.TWO_THIRDS))))    
    }
  }
  
  object Variable {
    def create(storage:StorageManager, varName:ModifiableExpression[String]) : Variable = 
      Variable (
          storage.create(VariableState.UNDEFINED),
          VisualProperties(varName, storage.create(LabelPositioning.BOTTOM), storage.create(new Point2D.Double(0,0)))
          )
  }
  
  object Vertex {
    def create (storage:StorageManager) = Vertex(storage.create (One.instance[Variable]), VisualProperties.create(storage))
  }
  
  object RhoClause {
    def create (storage:StorageManager) = RhoClause (storage.create(One.instance[Variable]), VisualProperties.create(storage))
  }
}
