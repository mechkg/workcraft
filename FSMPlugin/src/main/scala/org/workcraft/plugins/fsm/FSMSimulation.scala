package org.workcraft.plugins.fsm
import org.workcraft.gui.modeleditor.sim.SimulationModel
import org.workcraft.dependencymanager.advanced.user.Variable
import org.workcraft.scala.Expressions._
import org.workcraft.scala.effects.IO
import scalaz.Scalaz._

case class FSMSimulation(fsm: FSM) extends SimulationModel [State, (State, List[String])] {
  private val curState = Variable.create((fsm.initialState, List("a", "b", "c")))
  
  val state = curState.expr

  val enabled = state.map { case (curState, in) => (s: State) => {
      val arcs = fsm.postset(curState).filter(_._1 == s)
      
      in match {
	case Nil => arcs.exists(_._2 == "")
	case x => arcs.exists( a => (a._2 == x.head) || (a._2 == ""))
      }
    }
  }

  def fire(s: State) = curState.update { case (_, input) => (s, input.tail) }
    
  def name (e: State) = fsm.labels(e)

  def setState(state: (State, List[String])) = curState.set(state)
}
