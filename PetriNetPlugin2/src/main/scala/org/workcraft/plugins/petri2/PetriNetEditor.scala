package org.workcraft.plugins.petri2

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
import org.workcraft.services.Undo
import org.workcraft.services.UndoAction

import scalaz.Scalaz._
import scalaz._

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
              pushUndo ("change name") >>=|
              net.names.set(names - oldName + ((name, p))) >>=|
              net.labelling.update(_ + ((p, name))) >| None
            }
          } else
            ioPure.pure(None)).join
      } else ioPure.pure(Some("Names must be non-empty Latin alphanumeric strings not starting with a digit.")))
  }
  
  def tokens(p: Place) = ModifiableExpressionWithValidation[Int, String](
      net.tokens(p),
      x => if (x < 0) ioPure.pure{Some("Token count cannot be negative.")} else pushUndo ("change token count") >>=| net.marking.update(_ + (p -> x)) >| None
      )    

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

        val componentImages = treeSequence(comp.map(c => (componentImage(c, settings) <**> componentPosition(c))((img, pos) 
            => (color: Node => Colorisation, off: Set[Node], v: Point2D.Double) => img.cgc.applyColorisation(color(c)).transform(makeTransform(if (off.contains(c)) pos + v else pos)))))

        (arcImages <|*|> componentImages)
      }).join.map{ case (ai, ci) => (color: Node => Colorisation, off: Set[Node], v: Point2D.Double) => treeFold(GraphicalContent.Empty)(_.compose(_), ai.map(_(color))).compose(treeFold(GraphicalContent.Empty)(_.compose(_), ci.map(_(color, off, v))))  }
    })
    
  def image(colorisation: Node => Colorisation, offsetNodes: Set[Node], offsetValue: Point2D.Double): Expression[GraphicalContent] = imageV map (_(colorisation, offsetNodes, offsetValue))

  def componentImage(c: Component, settings: CommonVisualSettings): Expression[BoundedColorisableGraphicalContent] = c match {
    case p: Place => VisualPlace.image(net.tokens(p), net.label(p), settings)
    case t: Transition => VisualTransition.image(net.label(t), settings)
  }

  def arcImage(a: Arc): Expression[ConnectionGui] = for {
    visualArcs <- net.visualArcs;
    t1 <- touchable(a.from);
    t2 <- touchable(a.to);
    ap1 <- componentPosition(a.from);
    ap2 <- componentPosition(a.to)
  } yield {
    VisualConnectionGui.getConnectionGui(VisualArc.properties, VisualConnectionContext.makeContext(t1, ap1, t2, ap2), visualArcs(a))
  }

  def makeTransform(p: Point2D.Double) = AffineTransform.getTranslateInstance(p.getX, p.getY)

  def componentTransform(c: Component) = componentPosition(c).map(makeTransform(_))

  def componentPosition(c: Component) = net.layout.map(_(c))

  def componentX(c: Component): ModifiableExpression[Double] =
    ModifiableExpression(net.layout.map(_(c).getX), x => net.layout.update(m => m + (c -> new Point2D.Double(x, m(c).getY))))

  def touchable(n: Node) = n match {
    case p: Place => (VisualPlace.touchable <**> componentTransform(p))((touchable, xform) => touchable.transform(xform))
    case t: Transition => (VisualTransition.touchable <**> componentTransform(t))((touchable, xform) => touchable.transform(xform))
    case a: Arc => arcImage(a).map(_.shape.touchable)
  }

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
    image(_,_,_),
    List(KeyBinding("Delete selection", KeyEvent.VK_DELETE, KeyEventType.KeyPressed, Set(), pushUndo("delete nodes") >>=| selection.eval >>= (sel => selection.update(_ -- sel) >>=| net.deleteNodes(sel)))))

  private val connectionManager = new ConnectionManager[Component] {
    def connect(node1: Component, node2: Component): Either[InvalidConnectionException, IO[Unit]] = (node1, node2) match {
      case (from: Place, to: Transition) => Right(pushUndo("create arc") >>=| net.createConsumerArc(from, to) >| Unit)
      case (from: Transition, to: Place) => Right(pushUndo("create arc") >>=| net.createProducerArc(from, to) >| Unit)
      case (_: Place, _: Place) => Left(new InvalidConnectionException("Arcs between places are invalid"))
      case (_: Transition, _: Transition) => Left(new InvalidConnectionException("Arcs between transitions are invalid"))
    }
  }

  private def connectionTool =
    GenericConnectionTool[Component](net.components, touchable(_), componentPosition(_), connectionManager, (f => image({
      case _: Arc => Colorisation.Empty
      case c: Component => f(c)
    }, Set(), new Point2D.Double(0,0))))

  private def placeGeneratorTool =
    NodeGeneratorTool(Button("Place", "images/icons/svg/place.svg", Some(KeyEvent.VK_P)).unsafePerformIO, image(_ => Colorisation(None, None), Set(), new Point2D.Double(0,0)), pushUndo("create place") >>=| net.createPlace(_) >| Unit)

  private def transitionGeneratorTool =
    NodeGeneratorTool(Button("Transition", "images/icons/svg/transition.svg", Some(KeyEvent.VK_T)).unsafePerformIO, image(_ => Colorisation(None, None), Set(), new Point2D.Double(0,0)), pushUndo ("create transition") >>=| net.createTransition(_) >| Unit)

  def tools = NonEmptyList(selectionTool, connectionTool, placeGeneratorTool, transitionGeneratorTool)
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
  
  def pushUndo(description: String) = pushState(description, undoStack) >>=| redoStack.update (_ => Nil)

  def popUndo: IO[Unit] = undoStack.eval >>= ({
    case top::rest => pushState (top.description, redoStack) >>=| loadState(top) >>=| undoStack.update(_ => rest)
    case _ => ioPure.pure{}
  })
  
  def popRedo: IO[Unit] = redoStack.eval >>= ({
    case top::rest => pushState (top.description, undoStack) >>=| loadState(top) >>=| redoStack.update(_ => rest)
    case _ => ioPure.pure{}
  })

  val undo = Some(Undo(
      undoStack.map({ case top :: _ => Some(UndoAction(top.description, popUndo)); case x => None }),
      redoStack.map({ case top :: _ => Some(UndoAction(top.description, popRedo)); case x => None })
      ))
}