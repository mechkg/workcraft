package org.workcraft.gui.modeleditor.sim

import org.workcraft.scala.effects.IO
import org.workcraft.scala.Expressions._

trait SimulationModel[Event, State] {
  def currentState: Expression[State]
  def enabled: Expression[Event => Boolean]
  
  val trace: Expression[Trace[Event, State]]
  
  def toString (e: Event) : String
  def fire(event : Event) : IO[Unit]
}