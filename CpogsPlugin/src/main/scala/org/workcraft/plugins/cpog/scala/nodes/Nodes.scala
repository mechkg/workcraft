import org.workcraft.plugins.cpog.scala.VisualProperties
import org.workcraft.dependencymanager.advanced.user.StorageManager
import org.workcraft.plugins.cpog.optimisation.expressions.One
import org.workcraft.plugins.cpog.optimisation.BooleanFormula
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression
import org.workcraft.plugins.cpog.optimisation.BooleanVariable
import org.workcraft.dependencymanager.advanced.core.GlobalCache._
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanVisitor
import org.workcraft.plugins.cpog.VariableState

package org.workcraft.plugins.cpog.scala.components {

  sealed abstract class Node
  sealed abstract class Component extends Node
   
  case class Arc extends Node
  
  case class Vertex(condition: ModifiableExpression[BooleanFormula], visualProperties:VisualProperties) extends Component
   
  case class Variable(state:ModifiableExpression[VariableState], label: ModifiableExpression[String], visualProperties:VisualProperties) 
  			extends Component with BooleanVariable with Comparable[Variable] {
  
    override def getLabel : String = eval(label)
  
    override def compareTo(other:Variable) : Int = getLabel.compareTo(other.getLabel)
    
    override def accept [T] (visitor:BooleanVisitor[T]) : T = visitor.visit(this)
  
  }
  
  object Test {
    def main(args:Array[String]) {
      val x = Vertex(null, null)
          
      def govno (x:Node) = {
      x match {
        case y : Component => y match {
          case v : Vertex => 8
        }
        case qwe:Variable => 8
         
        
      }
      }
      
      println (govno(x))
    }
  }
  
  /*
   * public class Variable implements Comparable<Variable>, BooleanVariable, Node, Component
{
	public final ModifiableExpression<VariableState> state;
	public final ModifiableExpression<String> label;
	public final VisualComponent visualVar;

	public Variable(ModifiableExpression<VariableState> state, ModifiableExpression<String> label, VisualComponent visualVar) {
		this.state = state;
		this.label = label;
		this.visualVar = visualVar;
	}
	
	public static Variable make(StorageManager storage, ModifiableExpression<String> varName) {
		return new Variable
				( storage.create(VariableState.UNDEFINED)
				, varName
				, new VisualComponent(storage.<Point2D>create(new Point2D.Double(0, 0)), varName, storage.create(LabelPositioning.BOTTOM)));
	}
	
	public ModifiableExpression<VariableState> state()
	{
		return state;
	}	

	@Override
	public int compareTo(Variable o)
	{
		return getLabel().compareTo(o.getLabel());
	}
	
	@Override
	public String getLabel() {
		return eval(label);
	}

	@Override
	public <T> T accept(BooleanVisitor<T> visitor) {
		return visitor.visit(this);
	}

	@Override
	public <T> T accept(NodeVisitor<T> visitor) {
		return visitor.visitComponent(this);
	}

	@Override
	public <T> T accept(ComponentVisitor<T> visitor) {
		return visitor.visitVariable(this);
	}
}

   */
  
  object Vertex {
    def create (storage:StorageManager) = Vertex(storage.create (One.instance), VisualProperties.create(storage))
  }
  
  
  
  /* 
/*
*
* Copyright 2008,2009 Newcastle University
*
* This file is part of Workcraft.
* 
* Workcraft is free software: you can redistribute it and/or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* Workcraft is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with Workcraft.  If not, see <http://www.gnu.org/licenses/>.
*
*/

package org.workcraft.plugins.cpog.scala.nodes;

import org.workcraft.dependencymanager.advanced.user.ModifiableExpression;
import org.workcraft.dependencymanager.advanced.user.StorageManager;
import org.workcraft.plugins.cpog.optimisation.BooleanFormula;
import org.workcraft.plugins.cpog.optimisation.expressions.One;

public class Vertex implements Node, Component
{
	public static Vertex make(StorageManager storage) {
		return new Vertex(storage.<BooleanFormula>create(One.instance()), VisualComponent.make(storage));
	}
	
	public Vertex(ModifiableExpression<BooleanFormula> condition, VisualComponent visualInfo) {
		this.condition = condition;
		this.visualInfo = visualInfo;
	}

	public final ModifiableExpression<BooleanFormula> condition;
	public final VisualComponent visualInfo;
	@Override
	public <T> T accept(NodeVisitor<T> visitor) {
		return visitor.visitComponent(this);
	}
	
	@Override
	public <T> T accept(ComponentVisitor<T> visitor) {
		return visitor.visitVertex(this);
	}
}
*/
}