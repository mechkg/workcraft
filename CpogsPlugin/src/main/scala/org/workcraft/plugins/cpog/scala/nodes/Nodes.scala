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

package org.workcraft.plugins.cpog.scala.nodes {
 
  sealed abstract class Node
  sealed abstract case class Component(visualProperties:VisualProperties) extends Node
   
  case class Arc extends Node
  
  case class Vertex(condition: ModifiableExpression[BooleanFormula], override val visualProperties:VisualProperties) extends Component (visualProperties)
   
  case class Variable(state:ModifiableExpression[VariableState], label: ModifiableExpression[String], override val visualProperties:VisualProperties) 
  			extends Component (visualProperties) with BooleanVariable with Comparable[Variable] {
  
    override def getLabel : String = eval(label)
  
    override def compareTo(other:Variable) : Int = getLabel.compareTo(other.getLabel)
    
    override def accept [T] (visitor:BooleanVisitor[T]) : T = visitor.visit(this)
  }
  
  case class RhoClause(formula:ModifiableExpression[BooleanFormula], override val visualProperties:VisualProperties) extends Component (visualProperties)
  
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
    
    def conditionValue (vertex:Vertex) : Expression[BooleanFormula] = new ExpressionBase[BooleanFormula] {
      override def evaluate (context : EvaluationContext) = context.resolve(vertex.condition).accept(
    		  new BooleanReplacer (new HashMap[BooleanVariable, BooleanFormula]) {
    		    override def visit (node : BooleanVariable) : BooleanFormula = node match {
    		      case v : Variable => context.resolve(v.state) match {
    		        case VariableState.TRUE => One.instance
    		        case VariableState.FALSE => Zero.instance
    		        case _ => v
    		      }
    		    }
    		  }
          )
        }
  }
  
  object RhoClause {
    def create (storage:StorageManager) = RhoClause (storage.create(One.instance), VisualProperties.create(storage))
  }
}