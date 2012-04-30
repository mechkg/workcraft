package org.workcraft.gui.modeleditor.sim
import java.util.TimerTask
import javax.swing.SwingUtilities

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
import java.util.Timer
import java.awt.event.ActionEvent
import java.awt.event.ActionListener

case class Trace[Event](events: Seq[Event])

case class StateAnnotatedTrace[Event, State](initialState: State, events: Seq[(Event, State)])

case class MarkedTrace[Event, State](trace: StateAnnotatedTrace[Event, State], position: Int) {
  def goto(position: Int, applyState: State => IO[Unit]) = {
    applyState(if (position > 0) trace.events(position - 1)._2 else trace.initialState) >>=| ioPure.pure { MarkedTrace(trace, position) }
  }

  def !(e: Event, s: State) = MarkedTrace(StateAnnotatedTrace(trace.initialState, trace.events.take(position) :+ (e, s)), position + 1)
}

object Trace {
  def annotateWithState[Event, State](t: Trace[Event], state: IO[State], fire: Event => IO[Unit]) =
    state >>= (initialState => t.events.traverse(e => (fire(e) >>=| state).map((e, _))).map(StateAnnotatedTrace(initialState, _)))
}

class GenericSimulationToolInstance[Event, State](
  viewport: Viewport,
  hasFocus: Expression[Boolean],
  eventSources: Expression[Iterable[Event]],
  touchable: Event => Expression[Touchable],
  sim: SimulationModel[Event, State],
  val trace: ModifiableExpression[MarkedTrace[Event, State]],
  paint: ((Event => Colorisation), State) => Expression[GraphicalContent],
  message: Expression[State => Option[(String, Color)]]
) extends ModelEditorToolInstance {

  def fire(event: Event): IO[Unit] = sim.fire(event) >>=| sim.state.eval >>= (state => trace.update(_ ! (event, state)))

  def gotoState(position: Int) = trace.eval >>= (_.goto(position, sim.setState(_))) >>= (t => trace.set(t))

  val hitTester = HitTester.create(eventSources, touchable)
  val mouseListener = Some(new GenericSimulationToolMouseListener(node => ioPure.pure { hitTester.hitTest(node) }, sim.enabled.eval, (e: Event) => fire(e)))

  def keyBindings = List()

  def userSpaceContent = (sim.state <|**|> (sim.enabled, GenericSimulationTool.col)) >>= { case (state, enabled, col) => (paint(ev => if (enabled(ev)) col else Colorisation.Empty, state)) }

  def screenSpaceContent = (message <|*|> sim.state) >>= { case (m, s) => m(s) match {
    case Some ((message, color)) => GUI.editorMessage(viewport, color, message)
    case None => constant(GraphicalContent.Empty)
  }}

  val interfacePanel = Some(new SimControlPanel[Event, State](trace, (e: Event) => sim.name(e), gotoState(_)))
}

case class GenericSimulationTool[Event, State](
  eventSources: Expression[Iterable[Event]],
  touchable: Event => Expression[Touchable],
  sim: IO[SimulationModel[Event, State]],
  paint: ((Event => Colorisation), State) => Expression[GraphicalContent],
  message: Expression[State => Option[(String, Color)]]
) extends ModelEditorTool {

  def button = GenericSimulationTool.button

  def createInstance(env: ToolEnvironment) = for {
    sim <- sim;
    initialState <- sim.state.eval;
    trace <- newVar(MarkedTrace(StateAnnotatedTrace[Event, State](initialState, Seq()), 0))
  } yield new GenericSimulationToolInstance(env.viewport, env.hasFocus, eventSources, touchable, sim, trace, paint, message)

  def createInstanceWithGivenTrace(env: ToolEnvironment, trace: MarkedTrace[Event, State]) = for {
    sim <- sim;
    trace <- newVar(trace)
  } yield new GenericSimulationToolInstance(env.viewport, env.hasFocus, eventSources, touchable, sim, trace, paint, message)
}

object GenericSimulationTool {

  val button = new Button {
    override def hotkey = Some(KeyEvent.VK_M)
    override def icon = Some(GUI.createIconFromSvgUsingSettingsSize("images/icons/svg/start-green.svg").unsafePerformIO)
    override def label = "Simulation tool"
  }

  val t = Variable.create(0.0)

  // new Timer(30, new ActionListener {
  //   def actionPerformed(e: ActionEvent) = t.set(scala.math.sin(System.currentTimeMillis() / 200.0)).unsafePerformIO
  // }).start()

//  val tt = new Timer(true)
//  tt.scheduleAtFixedRate(new TimerTask { def run =SwingUtilities.invokeLater ( new Runnable { def run = t.set(scala.math.sin(System.currentTimeMillis() / 200.0)).unsafePerformIO })}, 0, 30)

  val col = t.map(t => Colorisation(Some(new Color(80 + (40 * t).toInt, 200 + (40 * t).toInt, 80 + (40 * t).toInt)), None))

  val highlightedColorisation = Colorisation(Some(new Color(240, 180, 40)), None)
}
