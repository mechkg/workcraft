package org.workcraft.plugins.petri2
import org.workcraft.scala.Expressions.ModifiableExpression
import org.workcraft.dependencymanager.advanced.user.Variable

sealed trait Node

class Component (val label: Variable[String]) extends Node
class Place(val tokens: Variable[Int], label: Variable[String]) extends Component(label)
class Transition(label: Variable[String]) extends Component(label)

class Arc extends Node
class ProducerArc (val from: Transition, val to: Place)
class ConsumerArc (val from: Place, val to: Transition)

class PetriNet {
  val places = Variable.create(List[Place]())
  val transitions = Variable.create(List[Transition]())
  val arcs = Variable.create(List[Arc]())
}