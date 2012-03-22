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
import javax.swing.JPanel
import javax.swing.JLabel
import javax.swing.Timer
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

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

  def userSpaceContent = (sim.currentState <|**|> (sim.enabled, GenericSimulationTool.col)) >>= { case (state, enabled, col) => (paint(ev => if (enabled(ev)) col else Colorisation.Empty, state)) }

  def screenSpaceContent = constant(GraphicalContent.Empty)
  val interfacePanel = Some(new SimControlPanel(sim.trace, (e:Event) => sim.toString(e)))
}

case class GenericSimulationTool[Event, State](
  eventSources: Expression[Iterable[Event]],
  touchable: Event => Expression[Touchable],
  sim: IO[SimulationModel[Event, State]],
  paint: ((Event => Colorisation), State) => Expression[GraphicalContent]) extends ModelEditorTool {
  def button = GenericSimulationTool.button
  def createInstance(env: ToolEnvironment) = sim >>= (sim => ioPure.pure { new GenericSimulationToolInstance(env.viewport, env.hasFocus, eventSources, touchable, sim, paint) })
}

object GenericSimulationTool {
  val button = new Button {
    override def hotkey = Some(KeyEvent.VK_M)
    override def icon = Some(GUI.createIconFromSvgUsingSettingsSize("images/icons/svg/start-green.svg").unsafePerformIO)
    override def label = "Simulation tool"
  }

  val t = Variable.create(0.0)

  new Timer(30, new ActionListener {
    def actionPerformed(e: ActionEvent) = t.set(scala.math.sin(System.currentTimeMillis() / 200.0)).unsafePerformIO
  }).start()

  val col = t.map(t => Colorisation(Some(new Color(80 + (40 * t).toInt, 200 + (40 * t).toInt, 80 + (40 * t).toInt)), None))

  val highlightedColorisation = Colorisation(Some(new Color(240, 180, 40)), None)
}