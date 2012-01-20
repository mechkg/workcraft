package org.workcraft.plugins.petri21
import org.workcraft.scala.Expressions.ModifiableExpression
import org.workcraft.dependencymanager.advanced.user.StorageManager

sealed trait Node

class Component (val label: ModifiableExpression[String]) extends Node
class Place(val tokens: ModifiableExpression[Int], label: ModifiableExpression[String]) extends Component(label)
class Transition(label: ModifiableExpression[String]) extends Component(label)

class Arc extends Node
class ProducerArc (val from: Transition, val to: Place)
class ConsumerArc (val from: Place, val to: Transition)

class PetriNet (val storage: StorageManager, _places: List[Place], _transitions: List[Transition], _arcs: List[Arc]) {
  val places : ModifiableExpression[List[Place]] = storage.create(_places)
  val transitions : ModifiableExpression[List[Transition]] = storage.create (_transitions)
  val arcs : ModifiableExpression[List[Arc]] = storage.create (_arcs)
}