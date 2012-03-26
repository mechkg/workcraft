package org.workcraft.plugins.fsm

case class State (label: String)

case class Arc (from: State, to: State, label: String)

case class FSM (states: List[State], arcs: List[Arc], initial: State)