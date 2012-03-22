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
import scalaz._
import Scalaz._

class GenericSimulationTool[Node, Event, State] (
  viewport: Viewport,
  hasFocus: Expression[Boolean],
  eventSources: Expression[Iterable[Node]],
  event: Node => Event,
  touchable: Node => Expression[Touchable],
  sim: SimulationModel[Event, State],
  paint: ((Node => Colorisation), State) => Expression[GraphicalContent]) extends ModelEditorTool {
  
  val hitTester = HitTester.create(eventSources, touchable)
  // val currentState = 
  

  val mouseListener = null //new GenericSimulationToolMouseListener()
  
  
  
  

  def button = GenericSimulationTool.button
  def keyBindings = List()
   
  def userSpaceContent = null /*impl.mouseOverNode >>= (mo =>
    (paint(n => if (Some(n) == mo) highlightedColorisation else Colorisation.Empty) <**>
      impl.connectingLineGraphicalContent(viewport))(_.compose(_))) */

  def screenSpaceContent = null //impl.screenSpaceContent(viewport, hasFocus)
  def interfacePanel = None
}

object GenericSimulationTool {
  def apply[Node, Event, State](  eventSources: Expression[Iterable[Node]],
  event: Node => Event,
  touchable: Node => Expression[Touchable],
  sim: SimulationModel[Event, State],
  paint: ((Node => Colorisation), State) => Expression[GraphicalContent]) = (env: ToolEnvironment) => 
      new GenericSimulationTool(env.viewport, env.hasFocus, eventSources, event, touchable, sim, paint)

  val highlightedColorisation = Colorisation(Some(new Color(99, 130, 191).brighter), None)
  
  val button =
    new Button {
      override def hotkey = Some(KeyEvent.VK_M)
      override def icon = Some(GUI.createIconFromSvgUsingSettingsSize("images/icons/svg/play.svg").unsafePerformIO)
      override def label = "Simulation tool"
    }
}
