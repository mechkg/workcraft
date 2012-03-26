package org.workcraft.plugins.petri2
import org.workcraft.gui.modeleditor.sim.SimulationModel
import org.workcraft.dependencymanager.advanced.user.Variable
import org.workcraft.scala.Expressions._
import org.workcraft.scala.effects.IO
import scalaz.Scalaz._

case class PetriNetSimulation(net: PetriNet) extends SimulationModel[Transition, Map[Place, Int]] {
  private val curState = Variable.create(net.marking)

  val state = curState.expr
  val enabled = curState.map(marking => ((t: Transition) => net.consumes(t).forall(p => marking(p) > 0)))

  def fire(t: Transition) = curState.update(marking => marking ++ net.produces(t).map(p => (p, marking(p) + 1)) ++ net.consumes(t).map(p => (p, marking(p) - 1))) 
    
  def name (e: Transition) = net.labelling(e)

  def setState(state: Map[Place, Int]) = curState.set(state)
}