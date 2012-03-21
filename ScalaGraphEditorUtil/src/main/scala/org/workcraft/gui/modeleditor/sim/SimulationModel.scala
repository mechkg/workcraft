package org.workcraft.gui.modeleditor.sim

import org.workcraft.scala.effects.IO

trait SimulationModel[Event, State] {
  def isEnabled (event: Event): IO[Boolean]   
  def fire(event : Event) : IO[Unit]
  
  def saveState : IO[State]
  def loadState(state : State) : IO[Unit]
}
