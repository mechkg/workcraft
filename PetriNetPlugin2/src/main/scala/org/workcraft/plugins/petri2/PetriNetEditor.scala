package org.workcraft.plugins.petri2

import java.awt.event.KeyEvent
import java.awt.geom.AffineTransform
import java.awt.geom.Point2D
import java.awt.Color
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

case class EditorState(description: String, net: VisualPetriNet, selection: Set[Node])

class PetriNetEditor(net: EditablePetriNet) extends ModelEditor {
  val selection = Variable.create(Set[Node]())

  def name(p: Place): ModifiableExpressionWithValidation[String, String] = {
    val expr = net.labelling.map(_(p))
    ModifiableExpressionWithValidation(
      expr, name => if (PetriNet.isValidName(name)) {
        (
          for {
            oldName <- expr.eval;
            names <- net.names.eval
          } yield if (name != oldName) {
            if (names.contains(name)) ioPure.pure(Some("The name '" + name + "' is already taken."))
            else {
              pushUndo("change name") >>=|
                net.names.set(names - oldName + ((name, p))) >>=|
                net.labelling.update(_ + ((p, name))) >| None
            }
          } else
            ioPure.pure(None)).join
      } else ioPure.pure(Some("Names must be non-empty Latin alphanumeric strings.")))
  }

  def tokens(p: Place) = ModifiableExpressionWithValidation[Int, String](
    net.tokens(p),
    x => if (x < 0) ioPure.pure { Some("Token count cannot be negative.") } else pushUndo("change token count") >>=| net.marking.update(_ + (p -> x)) >| None)

  def props: Expression[List[Expression[EditableProperty]]] = selection.map(_.toList.flatMap({
    case p: Place => List(
      IntegerProperty("Tokens", tokens(p)),
      StringProperty("Name", name(p)))
    case _ => Nil
  }).toList)

  val imageV: Expression[((Node => Colorisation), Set[Node], Point2D.Double) => GraphicalContent] =
    CommonVisualSettings.settings >>= (settings => {
      (net.components <**> net.arcs)((comp, a) => {

        val arcImages = treeSequence(a.map(a => arcImage(a).map(img => ((f: Node => Colorisation) => img.graphicalContent.applyColorisation(f(a))))))

        val componentImages = treeSequence(comp.map(c => (componentImage(c, settings) <**> componentPosition(c))((img, pos) => (color: Node => Colorisation, off: Set[Node], v: Point2D.Double) => img.cgc.applyColorisation(color(c)).transform(makeTransform(if (off.contains(c)) pos + v else pos)))))

        (arcImages <|*|> componentImages)
      }).join.map { case (ai, ci) => (color: Node => Colorisation, off: Set[Node], v: Point2D.Double) => treeFold(GraphicalContent.Empty)(_.compose(_), ai.map(_(color))).compose(treeFold(GraphicalContent.Empty)(_.compose(_), ci.map(_(color, off, v)))) }
    })

  def image(colorisation: Node => Colorisation, offsetNodes: Set[Node], offsetValue: Point2D.Double): Expression[GraphicalContent] = imageV map (_(colorisation, offsetNodes, offsetValue))

  def imageC(colorisation: Component => Colorisation) =
    (net.components <|**|> (net.arcs, CommonVisualSettings.settings)) >>= {
      case (comp, a, settings) => {
        treeSequence((a.map(a => arcImage(a).map(_.graphicalContent.applyColorisation(Colorisation.Empty))) ++
          comp.map(c => (componentImage(c, settings) <**> componentTransform(c))(_.transform(_).cgc.applyColorisation(colorisation(c)))))).map(_.foldLeft(GraphicalContent.Empty)(_.compose(_)))
      }
    }

  def imageForSelection(colorisation: Node => Colorisation, offsetNodes: Set[Node], offsetValue: Point2D.Double) =
    (net.components <|**|> (net.arcs, CommonVisualSettings.settings)) >>= {
      case (comp, a, settings) => {
        treeSequence((a.map(a => arcImageWithOffset(a, offsetNodes, offsetValue).map(_.graphicalContent.applyColorisation(colorisation(a)))) ++
          comp.map(c => (componentImage(c, settings) <**> componentTransformWithOffset(c, offsetNodes, offsetValue))(_.transform(_).cgc.applyColorisation(colorisation(c)))))).map(_.foldLeft(GraphicalContent.Empty)(_.compose(_)))
      }
    }

  def imageForSimulation(colorisation: Transition => Colorisation, marking: Map[Place, Int]) =
    (net.components <|**|> (net.arcs, CommonVisualSettings.settings)) >>= {
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
    }

  def componentImage(c: Component, settings: CommonVisualSettings): Expression[BoundedColorisableGraphicalContent] = c match {
    case p: Place => VisualPlace.image(net.tokens(p), net.label(p), settings)
    case t: Transition => VisualTransition.image(net.label(t), settings)
  }

  def arcImage(a: Arc): Expression[ConnectionGui] = arcImageWithOffset(a, Set(), new Point2D.Double(0, 0))

  def arcImageWithOffset(a: Arc, offsetNodes: Set[Node], offsetValue: Point2D.Double) = for {
    visualArcs <- net.visualArcs;
    t1 <- touchableWithOffset(a.from, offsetNodes, offsetValue);
    t2 <- touchableWithOffset(a.to, offsetNodes, offsetValue);
    ap1 <- componentPositionWithOffset(a.from, offsetNodes, offsetValue);
    ap2 <- componentPositionWithOffset(a.to, offsetNodes, offsetValue)
  } yield {
    VisualConnectionGui.getConnectionGui(VisualArc.properties, VisualConnectionContext.makeContext(t1, ap1, t2, ap2), visualArcs(a))
  }

  def makeTransform(p: Point2D.Double) = AffineTransform.getTranslateInstance(p.getX, p.getY)

  def componentTransform(c: Component) = componentPosition(c).map(makeTransform(_))

  def componentTransformWithOffset(c: Component, offsetNodes: Set[Node], offsetValue: Point2D.Double) = componentPositionWithOffset(c, offsetNodes, offsetValue).map(makeTransform(_))

  def componentPosition(c: Component) = net.layout.map(_(c))

  def componentPositionWithOffset(c: Component, offsetNodes: Set[Node], offsetValue: Point2D.Double) = componentPosition(c).lwmap(pos => if (offsetNodes.contains(c)) pos + offsetValue else pos)

  def componentX(c: Component): ModifiableExpression[Double] =
    ModifiableExpression(net.layout.map(_(c).getX), x => net.layout.update(m => m + (c -> new Point2D.Double(x, m(c).getY))))

  def touchable(n: Node) = touchableWithOffset(n, Set(), new Point2D.Double())

  def touchableWithOffset(n: Node, offsetNodes: Set[Node], offsetValue: Point2D.Double) = n match {
    case p: Place => (VisualPlace.touchable <**> componentTransformWithOffset(p, offsetNodes, offsetValue))((touchable, xform) => touchable.transform(xform))
    case t: Transition => (VisualTransition.touchable <**> componentTransformWithOffset(t, offsetNodes, offsetValue))((touchable, xform) => touchable.transform(xform))
    case a: Arc => arcImage(a).map(_.shape.touchable)
  }

  def toggleSelectionMarking (selection: Set[Node]) : IO[Unit] = net.marking.eval >>= ( marking => selection.traverse_ {
    case p:Place if (marking(p) == 0) => net.marking.update (_ + (p -> 1))
    case p:Place if (marking(p) == 1) => net.marking.update (_ + (p -> 0))
    case _ => IO.Empty
  })

  def toggleMarking (node: Node) : IO[Unit] = net.marking.eval >>= ( marking => node match {
    case p:Place if (marking(p) == 0) => net.marking.update (_ + (p -> 1))
    case p:Place if (marking(p) == 1) => net.marking.update (_ + (p -> 0))
    case _ => IO.Empty
  })

  def move(nodes: Set[Node], offset: Point2D.Double): IO[Unit] = pushUndo("move nodes") >>=| nodes.map({
    case c: Component => net.layout.update(l => l + (c -> (l(c) + offset)))
    case _ => IO.Empty
  }).traverse_(x => x)

  private def selectionTool = GenericSelectionTool[Node](
    net.nodes,
    selection,
    move,
    (_, x) => x,
    touchable(_),
    imageForSelection(_, _, _),
    List(
      KeyBinding("Delete selection", KeyEvent.VK_DELETE, KeyEventType.KeyPressed, Set(), pushUndo("delete nodes") >>=| selection.eval >>= (sel => selection.update(_ -- sel) >>=| net.deleteNodes(sel) >| None)),
      KeyBinding("Toggle marking", KeyEvent.VK_SPACE, KeyEventType.KeyPressed, Set(), pushUndo("toggle marking") >>=| (selection.eval >>= (sel => toggleSelectionMarking(sel))) >| None)
    ),
    Some (toggleMarking(_)))

  private val connectionManager = new ConnectionManager[Component] {
    def connect(node1: Component, node2: Component): Expression[Either[InvalidConnectionException, IO[Unit]]] = constant((node1, node2) match {
      case (from: Place, to: Transition) => Right(pushUndo("create arc") >>=| net.createConsumerArc(from, to) >| Unit)
      case (from: Transition, to: Place) => Right(pushUndo("create arc") >>=| net.createProducerArc(from, to) >| Unit)
      case (_: Place, _: Place) => Left(new InvalidConnectionException("Arcs between places are invalid"))
      case (_: Transition, _: Transition) => Left(new InvalidConnectionException("Arcs between transitions are invalid"))
    })
  }

  private val simToolMessage = (net.transitions.expr <**> net.transitionIncidence) ( (ts, ti) => {
    (marking:Map[Place, Int]) =>
      if (ts.forall(t => ti._2(t).forall(marking(_) == 0 )))
	Some(("The net is in a deadlock state: no transitions can be fired", Color.RED)) 
      else 
	Some(("Click on the highlighted transitions to fire them"), Color.BLACK)
  })

  private val simulationTool =
    GenericSimulationTool[Transition, Map[Place, Int]](
      net.transitions, 
      touchable(_), 
      net.saveState.eval.map(vpn => new PetriNetSimulation(vpn.net)), 
      imageForSimulation(_, _), 
      simToolMessage
      )

  private def connectionTool =
    GenericConnectionTool[Component](net.components, touchable(_), componentPosition(_), connectionManager, imageC(_))

  private def placeGeneratorTool =
    NodeGeneratorTool(Button("Place", "images/icons/svg/place.svg", Some(KeyEvent.VK_P)).unsafePerformIO, image(_ => Colorisation(None, None), Set(), new Point2D.Double(0, 0)), pushUndo("create place") >>=| net.createPlace(_) >| Unit)

  private def transitionGeneratorTool =
    NodeGeneratorTool(Button("Transition", "images/icons/svg/transition.svg", Some(KeyEvent.VK_T)).unsafePerformIO, image(_ => Colorisation(None, None), Set(), new Point2D.Double(0, 0)), pushUndo("create transition") >>=| net.createTransition(_) >| Unit)

  def tools = NonEmptyList(selectionTool, connectionTool, placeGeneratorTool, transitionGeneratorTool, simulationTool)
  def keyBindings = List()
  def button = new Button {
    def hotkey = Some(KeyEvent.VK_K)
    def icon = None
    def label = "Hi :-)"
  }

  val undoStack = Variable.create(List[EditorState]())
  val redoStack = Variable.create(List[EditorState]())

  def saveState(description: String): IO[EditorState] = (net.saveState.eval <**> selection.eval)(EditorState(description, _, _))

  def loadState(state: EditorState): IO[Unit] = net.loadState(state.net) >>=| selection.set(state.selection)

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

    case PropertyService => Some(props)

    case ShowTraceService => Some(new ShowTrace {
      def show(trace: List[String]) = (simulationTool, (env: ToolEnvironment) => net.saveState.eval.map(_.net) >>= (net => {

        val eventTrace = Trace(trace.map(s => net.names.get(s) match {
          case Some(t: Transition) => t
          case _ => throw new RuntimeException("No transition named " + s)
        }))

        val sim = PetriNetSimulation(net)

        Trace.annotateWithState[Transition, Map[Place, Int]](eventTrace, sim.state.eval, sim.fire(_)) >>= (trace => simulationTool.createInstanceWithGivenTrace(env, MarkedTrace(trace, 0)))
      }))
    })
  }
}
