package org.workcraft.plugins.petri2
import java.awt.geom.Point2D
import org.workcraft.scala.effects.IO
import org.workcraft.dom.visual.connections.StaticVisualConnectionData
import scalaz._
import Scalaz._

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
  val names = labelling.toList.map(_.swap).toMap

  def produces(t: Transition) = incidence._1(t)
  def consumes(t: Transition) = incidence._2(t)

  lazy val incidence: (Map[Transition, List[Place]], Map[Transition, List[Place]]) =
    arcs.foldLeft((Map[Transition, List[Place]]().withDefault(_ => List()), Map[Transition, List[Place]]().withDefault(_ => List())))({
      case ((prod, cons), arc) => arc match {
        case c: ConsumerArc => (prod, cons + (c.to -> (c.from :: cons(c.to))))
        case p: ProducerArc => (prod + (p.from -> (p.to :: prod(p.from))), cons)
      }
    })

  lazy val placeIncidence: (Map[Place, List[Transition]], Map[Place, List[Transition]]) =
    arcs.foldLeft((Map[Place, List[Transition]]().withDefault(_ => List()), Map[Place, List[Transition]]().withDefault(_ => List())))({
      case ((prod, cons), arc) => arc match {
        case c: ConsumerArc => (prod, cons + (c.from -> (c.to :: cons(c.from))))
        case p: ProducerArc => (prod + (p.to -> (p.from :: prod(p.to))), cons)
      }
    })
}

object PetriNet {
  val Empty = PetriNet(Map(), Map(), List(), List(), List())
  val namePattern = "[a-zA-Z_][0-9a-zA-Z_]*"
  def isValidName(s: String) = s.matches(namePattern)

  implicit val _semigroup = semigroup[PetriNet](
    (n1, n2) => PetriNet(n1.marking ++ n2.marking, n1.labelling ++ n2.labelling, n1.places ++ n2.places, n1.transitions ++ n2.transitions, n1.arcs ++ n2.arcs))
}

case class VisualPetriNet(net: PetriNet, layout: Map[Component, Point2D.Double], visualArcs: Map[Arc, StaticVisualConnectionData])

object VisualPetriNet {
  val Empty = VisualPetriNet(PetriNet.Empty, Map(), Map())
}