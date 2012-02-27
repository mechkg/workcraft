package org.workcraft.plugins.petri.tools

trait SimulationModel[M[_], Event, State] {
  def canFire(event : Event) : M[Boolean]
  def fire(event : Event) : M[Unit]
  def canUnfire(event : Event) : M[Boolean]
  def unfire(event : Event) : M[Unit]
  def saveState : M[State]
  def loadState(state : State) : M[Unit]
}
