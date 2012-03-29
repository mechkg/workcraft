package org.workcraft.plugins.fsm

import java.awt.event.KeyEvent
import java.awt.geom.AffineTransform
import java.awt.geom.Point2D
import org.workcraft.dependencymanager.advanced.user.Variable
import org.workcraft.dom.visual.connections.ConnectionGui
import org.workcraft.dom.visual.connections.VisualConnectionContext
import org.workcraft.dom.visual.connections.VisualConnectionGui
import org.workcraft.exceptions.InvalidConnectionException
import org.workcraft.graphics.Java2DDecoration.decoratePoint2D
import org.workcraft.graphics.BoundedColorisableGraphicalContent
import org.workcraft.graphics.Colorisation
import org.workcraft.graphics.GraphicalContent
import org.workcraft.gui.modeleditor.tools.ConnectionManager
import org.workcraft.gui.modeleditor.tools.Button
import org.workcraft.gui.modeleditor.tools.NodeGeneratorTool
import org.workcraft.gui.modeleditor.KeyBinding
import org.workcraft.gui.modeleditor.KeyEventType
import org.workcraft.gui.modeleditor.ModelEditor
import org.workcraft.gui.propertyeditor.integer.IntegerProperty
import org.workcraft.gui.propertyeditor.string.StringProperty
import org.workcraft.gui.propertyeditor.EditableProperty
import org.workcraft.gui.CommonVisualSettings
import org.workcraft.scala.Expressions._
import org.workcraft.scala.effects.IO._
import org.workcraft.scala.effects.IO
import org.workcraft.scala.grapheditor.tools.GenericConnectionTool
import org.workcraft.scala.grapheditor.tools.GenericSelectionTool
import scalaz.Scalaz._
import scalaz._
import org.workcraft.gui.modeleditor.sim.GenericSimulationTool
import org.workcraft.services.Service
import org.workcraft.services.EditorScope
import org.workcraft.gui.modeleditor.UndoService
import org.workcraft.gui.modeleditor.Undo
import org.workcraft.gui.modeleditor.UndoAction
import org.workcraft.gui.modeleditor.PropertyService
import org.workcraft.gui.modeleditor.ShowTraceService
import org.workcraft.gui.modeleditor.ShowTrace
import org.workcraft.gui.modeleditor.tools.ToolEnvironment
import org.workcraft.gui.modeleditor.sim.Trace
import org.workcraft.gui.modeleditor.sim.MarkedTrace
import org.workcraft.plugins.petri2.PetriNet
import org.workcraft.plugins.petri2.VisualArc
import javax.swing.JOptionPane

case class EditorState(description: String, net: VisualFSM, selection: Set[Node])

class FSMEditor(fsm: EditableFSM) extends ModelEditor {
  val selection = Variable.create(Set[Node]())

  val props = new FSMProperties(fsm, selection, pushUndo)

  def imageForConnection(colorisation: State => Colorisation) =
    (fsm.states.map(_.list) <|**|> (fsm.arcs, CommonVisualSettings.settings)) >>= {
      case (states, arcs, settings) => {
        treeSequence((arcs.map(a => arcImage(a).map(_.graphicalContent.applyColorisation(Colorisation.Empty))) ++
          states.map(s => (stateImage(s, settings) <**> stateTransform(s))(_.transform(_).cgc.applyColorisation(colorisation(s)))))).map(_.foldLeft(GraphicalContent.Empty)(_.compose(_)))
      }
    }

  def imageForSelection(colorisation: Node => Colorisation, offsetNodes: Set[Node], offsetValue: Point2D.Double) =
    (fsm.states.map(_.list) <|**|> (fsm.arcs, CommonVisualSettings.settings)) >>= {
      case (comp, a, settings) => {
        treeSequence((a.map(a => arcImageWithOffset(a, offsetNodes, offsetValue).map(_.graphicalContent.applyColorisation(colorisation(a)))) ++
          comp.map(c => (stateImage(c, settings) <**> stateTransformWithOffset(c, offsetNodes, offsetValue))(_.transform(_).cgc.applyColorisation(colorisation(c)))))).map(_.foldLeft(GraphicalContent.Empty)(_.compose(_)))
      }
    }

  /*def imageForSimulation(colorisation: State => Colorisation, curState: State) =
    (fsm.states <|**|> (net.arcs, CommonVisualSettings.settings)) >>= {
      case (comp, a, settings) => {
        treeSequence((a.map(a => arcImage(a).map(_.graphicalContent.applyColorisation(Colorisation.Empty))) ++
          comp.map(c => ((c match {
            case t: Transition => VisualTransition.image(net.label(t), settings)
            case p: Place => VisualPlace.image(constant(marking(p)), net.label(p), settings)
          }) <**> componentTransform(c))(_.transform(_).cgc.applyColorisation(c match {
            case t: Transition => colorisation(t)
            case p: Place => Colorisation.Empty
          }))))).map(_.foldLeft(GraphicalContent.Empty)(_.compose(_)))
      }
    }*/

  def stateImage(state: State, settings: CommonVisualSettings): Expression[BoundedColorisableGraphicalContent] =
    (fsm.labels.map(_(state)) <***> (fsm.initialState.map(_ == state), fsm.finalStates.map(_.contains(state))))((l, i, t) => FSMGraphics.stateImage(l, i, t, settings))

  def arcImage(a: Arc): Expression[ConnectionGui] = arcImageWithOffset(a, Set(), new Point2D.Double(0, 0))

  def arcImageWithOffset(a: Arc, offsetNodes: Set[Node], offsetValue: Point2D.Double) = for {
    visualArcs <- fsm.visualArcs;
    t1 <- touchableWithOffset(a.from, offsetNodes, offsetValue);
    t2 <- touchableWithOffset(a.to, offsetNodes, offsetValue);
    ap1 <- statePositionWithOffset(a.from, offsetNodes, offsetValue);
    ap2 <- statePositionWithOffset(a.to, offsetNodes, offsetValue)
  } yield {
    VisualConnectionGui.getConnectionGui(VisualArc.properties, VisualConnectionContext.makeContext(t1, ap1, t2, ap2), visualArcs(a))
  }

  def makeTransform(p: Point2D.Double) = AffineTransform.getTranslateInstance(p.getX, p.getY)

  def stateTransform(s: State) = statePosition(s).map(makeTransform(_))

  def stateTransformWithOffset(s: State, offsetNodes: Set[Node], offsetValue: Point2D.Double) = statePositionWithOffset(s, offsetNodes, offsetValue).map(makeTransform(_))

  def statePosition(s: State) = fsm.layout.map(_(s))

  def statePositionWithOffset(c: State, offsetNodes: Set[Node], offsetValue: Point2D.Double) = statePosition(c).lwmap(pos => if (offsetNodes.contains(c)) pos + offsetValue else pos)

  def touchable(n: Node) = touchableWithOffset(n, Set(), new Point2D.Double())

  def touchableWithOffset(n: Node, offsetNodes: Set[Node], offsetValue: Point2D.Double) = n match {
    case s: State => (FSMGraphics.stateTouchable <**> stateTransformWithOffset(s, offsetNodes, offsetValue))((touchable, xform) => touchable.transform(xform))
    case a: Arc => arcImage(a).map(_.shape.touchable)
  }

  def move(nodes: Set[Node], offset: Point2D.Double): IO[Unit] = pushUndo("move nodes") >>=| nodes.map({
    case c: State => fsm.layout.update(l => l + (c -> (l(c) + offset)))
    case _ => IO.Empty
  }).traverse_(x => x)

  private def selectionTool = GenericSelectionTool[Node](
    fsm.nodes,
    selection,
    move,
    (_, x) => x,
    touchable(_),
    imageForSelection(_, _, _),
    List(KeyBinding("Delete selection", KeyEvent.VK_DELETE, KeyEventType.KeyPressed, Set(), selection.eval >>= { sel =>
      fsm.deleteNodes(sel) >>= {
        case Right(io) => (pushUndo("delete nodes") >>=| selection.update(_ -- sel) >>=| io) >| None
        case Left(message) => ioPure.pure { (Some(message)) }
      }
    })))

  private val connectionManager = new ConnectionManager[State] {
    def connect(node1: State, node2: State): Either[InvalidConnectionException, IO[Unit]] = Right(pushUndo("create arc") >>=| fsm.createArc(node1, node2) >| Unit)
  }

  /*private val simulationTool =
    GenericSimulationTool[Transition, Map[Place, Int]](net.transitions, touchable(_), net.saveState.eval.map(vpn => new PetriNetSimulation(vpn.net)), imageForSimulation(_, _), Some("Click on the highlighted transitions to fire them"), Some("The net is in a deadlock state: no transitions can be fired"))*/

  private def connectionTool =
    GenericConnectionTool[State](fsm.states.map(_.list), touchable(_), statePosition(_), connectionManager, imageForConnection(_))

  private def stateGeneratorTool =
    NodeGeneratorTool(Button("State", "images/icons/svg/place_empty.svg", Some(KeyEvent.VK_T)).unsafePerformIO, imageForConnection(_ => Colorisation(None, None)), pushUndo("create state") >>=| fsm.createState(_) >| Unit)

  def tools = NonEmptyList(selectionTool, connectionTool, stateGeneratorTool)
  def keyBindings = List()

  val undoStack = Variable.create(List[EditorState]())
  val redoStack = Variable.create(List[EditorState]())

  def saveState(description: String): IO[EditorState] = (fsm.saveState.eval <**> selection.eval)(EditorState(description, _, _))

  def loadState(state: EditorState): IO[Unit] = fsm.loadState(state.net) >>=| selection.set(state.selection)

  def pushState(description: String, stack: ModifiableExpression[List[EditorState]]): IO[Unit] = (saveState(description) >>= (state => stack.update(s => (state :: s).take(100))))

  def pushUndo(description: String) = pushState(description, undoStack) >>=| redoStack.update(_ => Nil)

  def popUndo: IO[Unit] = undoStack.eval >>= ({
    case top :: rest => pushState(top.description, redoStack) >>=| loadState(top) >>=| undoStack.update(_ => rest)
    case _ => ioPure.pure {}
  })

  def popRedo: IO[Unit] = redoStack.eval >>= ({
    case top :: rest => pushState(top.description, undoStack) >>=| loadState(top) >>=| redoStack.update(_ => rest)
    case _ => ioPure.pure {}
  })

  def implementation[T](service: Service[EditorScope, T]) = service match {
    case UndoService => Some(Undo(
      undoStack.map({ case top :: _ => Some(UndoAction(top.description, popUndo)); case x => None }),
      redoStack.map({ case top :: _ => Some(UndoAction(top.description, popRedo)); case x => None })))

    case PropertyService => Some(props.props)
  }
}