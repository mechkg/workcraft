package org.workcraft.plugins.petri21

sealed trait Node

class Arc (val first: Component, val second: Component) extends Node
class Component (val label: String) extends Node

class Place(val tokens:Int, label: String) extends Component(label)
class Transition(label: String) extends Component(label)


class PetriNet {
    
}