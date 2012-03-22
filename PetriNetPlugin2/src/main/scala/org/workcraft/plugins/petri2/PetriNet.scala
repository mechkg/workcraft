package org.workcraft.plugins.petri2
import java.awt.geom.Point2D
import org.workcraft.scala.effects.IO
import org.workcraft.dom.visual.connections.StaticVisualConnectionData

sealed trait Node

sealed trait Component extends Node
class Place private[petri2] extends Component
class Transition private[petri2] extends Component

sealed trait Arc extends Node {
  def from: Component
  def to: Component
}

case class ProducerArc private[petri2] (from: Transition, to: Place) extends Arc
case class ConsumerArc private[petri2] (from: Place, to: Transition) extends Arc

case class PetriNet(marking: Map[Place, Int], labelling: Map[Component, String], places: List[Place], transitions: List[Transition], arcs: List[Arc]) {
  val names = labelling.toList.map({ case (a, b) => (b, a) }).toMap
  
  def produces (t: Transition) = incidence._1(t)
  def consumes (t: Transition) = incidence._2(t)

  lazy val incidence: (Map[Transition, List[Place]], Map[Transition, List[Place]]) =
    arcs.foldRight((Map[Transition, List[Place]]().withDefault(_ => List()), Map[Transition, List[Place]]().withDefault(_ => List())))({
      case (arc, (prod, cons)) => arc match {
        case c: ConsumerArc => (prod, cons + (c.to -> (c.from :: cons(c.to))))
        case p: ProducerArc => (prod + (p.from -> (p.to :: prod(p.from))), cons)
      }
    })
}

object PetriNet {
  val Empty = PetriNet(Map(), Map(), List(), List(), List())
  val namePattern = "[a-zA-Z_][0-9a-zA-Z_]*"
  def isValidName(s: String) = s.matches(namePattern)
}

case class VisualPetriNet(net: PetriNet, layout: Map[Component, Point2D.Double], visualArcs: Map[Arc, StaticVisualConnectionData])

object VisualPetriNet {
  val Empty = VisualPetriNet(PetriNet.Empty, Map(), Map())
}