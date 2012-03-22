package org.workcraft.gui.modeleditor.sim

import org.workcraft.scala.effects.IO._
import org.workcraft.scala.Expressions._
import java.awt.geom.Point2D
import java.awt.Color
import org.workcraft.graphics.Colorisation
import org.workcraft.graphics.Touchable
import org.workcraft.graphics.GraphicalContent
import org.workcraft.gui.modeleditor.tools.ConnectionManager
import org.workcraft.gui.modeleditor.tools.ModelEditorTool
import org.workcraft.gui.modeleditor.tools.Button
import java.awt.event.KeyEvent
import org.workcraft.gui.GUI
import org.workcraft.gui.modeleditor.tools.GenericConnectionToolImpl
import org.workcraft.gui.modeleditor.Viewport
import org.workcraft.gui.modeleditor.tools.ToolEnvironment
import org.workcraft.scala.grapheditor.tools.HitTester
import org.workcraft.dependencymanager.advanced.user.Variable
import scalaz.Scalaz._
import org.workcraft.gui.modeleditor.tools.ModelEditorToolInstance
import org.workcraft.gui.modeleditor.tools.ToolEnvironment
import org.workcraft.scala.effects.IO

class GenericSimulationToolInstance[Event, State](
  viewport: Viewport,
  hasFocus: Expression[Boolean],
  eventSources: Expression[Iterable[Event]],
  touchable: Event => Expression[Touchable],
  sim: SimulationModel[Event, State],
  paint: ((Event => Colorisation), State) => Expression[GraphicalContent]) extends ModelEditorToolInstance {

  val hitTester = HitTester.create(eventSources, touchable)
  val mouseListener = Some(new GenericSimulationToolMouseListener(node => ioPure.pure { hitTester.hitTest(node) }, sim))
  
  def keyBindings = List()

  def userSpaceContent = (sim.currentState <|*|> sim.enabled) >>= { case (state, enabled) => (paint(ev => if (enabled(ev)) GenericSimulationTool.highlightedColorisation else Colorisation.Empty, state)) }

  def screenSpaceContent = constant(GraphicalContent.Empty)
  def interfacePanel = None
}

case class GenericSimulationTool[Event, State] (
  eventSources: Expression[Iterable[Event]],
  touchable: Event => Expression[Touchable],
  sim: IO[SimulationModel[Event, State]],
  paint: ((Event => Colorisation), State) => Expression[GraphicalContent]) extends ModelEditorTool {
  def button = GenericSimulationTool.button
  def createInstance(env: ToolEnvironment) = sim >>= (sim => ioPure.pure { new GenericSimulationToolInstance (env.viewport, env. hasFocus, eventSources, touchable, sim, paint) })
}

object GenericSimulationTool {
  val button = new Button {
      override def hotkey = Some(KeyEvent.VK_M)
      override def icon = Some(GUI.createIconFromSvgUsingSettingsSize("images/icons/svg/start.svg").unsafePerformIO)
      override def label = "Simulation tool"
    }
  
  val highlightedColorisation = Colorisation (Some(new Color(240, 180, 40)), None)
}