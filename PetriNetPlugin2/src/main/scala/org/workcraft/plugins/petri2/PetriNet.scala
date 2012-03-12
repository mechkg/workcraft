package org.workcraft.plugins.petri2
import org.workcraft.dependencymanager.advanced.user.Variable
import org.workcraft.services.Model
import org.workcraft.services.Service
import org.workcraft.services.ModelScope
import org.workcraft.gui.modeleditor.EditorService
import org.workcraft.gui.modeleditor.ModelEditor
import scalaz.NonEmptyList
import scalaz._
import Scalaz._
import org.workcraft.scala.effects.IO
import org.workcraft.scala.effects.IO._
import org.workcraft.gui.modeleditor.tools.ModelEditorTool
import org.workcraft.graphics.GraphicalContent
import org.workcraft.gui.modeleditor.tools.Button
import org.workcraft.gui.modeleditor.KeyBinding
import java.awt.event.KeyEvent
import org.workcraft.gui.modeleditor.Modifier
import javax.swing.JOptionPane
import org.workcraft.gui.modeleditor.tools.DummyMouseListener
import org.workcraft.gui.modeleditor.MouseButton
import java.awt.geom.Point2D
import org.workcraft.gui.modeleditor.KeyEventType
import org.workcraft.scala.grapheditor.tools.GenericSelectionTool
import org.workcraft.scala.Expressions._
import org.workcraft.graphics.Colorisation
import java.awt.geom.AffineTransform
import org.workcraft.gui.modeleditor.tools.NodeGeneratorTool
import org.workcraft.graphics.ColorisableGraphicalContent
import org.workcraft.graphics.BoundedColorisableGraphicalContent
import org.workcraft.gui.CommonVisualSettings
import org.workcraft.scala.grapheditor.tools.GenericConnectionTool
import org.workcraft.gui.propertyeditor.EditableProperty
import org.workcraft.gui.modeleditor.tools.ConnectionManager
import org.workcraft.exceptions.InvalidConnectionException
import org.workcraft.dom.visual.connections.StaticVisualConnectionData
import org.workcraft.dom.visual.connections.Polyline
import org.workcraft.dom.visual.connections.VisualConnectionGui
import org.workcraft.dom.visual.connections.VisualConnectionProperties
import java.awt.Color
import java.awt.BasicStroke
import org.workcraft.dom.visual.connections.VisualConnectionContext
import org.workcraft.dom.visual.connections.ConnectionGui
import org.workcraft.gui.propertyeditor.integer.IntegerProperty
import org.workcraft.services.ModelService

sealed trait Node

sealed trait Component extends Node
class Place private[petri2] extends Component
class Transition private[petri2] extends Component

sealed trait Arc extends Node {
  def from: Component
  def to: Component
}
case class ProducerArc private[petri2] (from: Transition, to: Place) extends Arc
case class ConsumerArc private[petri2] (from: Place, to: Transition) extends Arc

object PetriNetSnapshotService extends ModelService[PetriNetSnapshot]

case class PetriNetSnapshot (marking: Map[Place, Int], labelling: Map[Component, String], places: List[Place], transitions: List[Transition], arcs: List[Arc])

object PetriNetSnapshot {
  val Empty = PetriNetSnapshot (Map(), Map(), List(), List(), List())
}

class PetriNet (initialState: PetriNetSnapshot = PetriNetSnapshot.Empty) {
  val marking = Variable.create(initialState.marking)
  val labelling = Variable.create(initialState.labelling)
  val places = Variable.create(initialState.places)
  val transitions = Variable.create(initialState.transitions)
  val arcs = Variable.create(initialState.arcs)

  val nodes: Expression[List[Node]] = (places.expr <***> (transitions, arcs))((p, t, a) => a ++ t ++ p)

  val components: Expression[List[Component]] = (places.expr <**> transitions)((p, t) => p ++ t)

  def tokens(place: Place) = ModifiableExpression (marking.map(_(place)), (t:Int) => marking.update( _ + (place -> t)))
  def label(c: Component) = labelling.map(_(c))

  private def newPlace = ioPure.pure { new Place }
  private def newTransition = ioPure.pure { new Transition }
  private def newProducerArc(from: Transition, to: Place) = ioPure.pure { new ProducerArc(from, to) }
  private def newConsumerArc(from: Place, to: Transition) = ioPure.pure { new ConsumerArc(from, to) }

  def createPlace: IO[Place] = for {
    p <- newPlace;
    _ <- places.update(p :: _);
    _ <- marking.update(_ + (p -> 0));
    _ <- labelling.update(_ + (p -> "Pitsot"))
  } yield p

  def createTransition: IO[Transition] = for {
    t <- newTransition;
    _ <- transitions.update(t :: _);
    _ <- labelling.update(_ + (t -> "Sto"))
  } yield t

  def createProducerArc(from: Transition, to: Place) = for {
    a <- newProducerArc(from, to);
    _ <- arcs.update(a :: _)
  } yield a

  def createConsumerArc(from: Place, to: Transition) = for {
    a <- newConsumerArc(from, to);
    _ <- arcs.update(a :: _)
  } yield a
  
  def snapshot = for {
    places <- places;
    transitions <- transitions;
    arcs <- arcs;
    labelling <- labelling;
    marking <- marking
  } yield PetriNetSnapshot (marking, labelling, places, transitions, arcs)
}

class PetriNetEditor(model: PetriNetModel) extends ModelEditor {

  def props: Expression[List[EditableProperty]] = model.selection.map (_.flatMap ({
    case p:Place => Some(IntegerProperty("Tokens", model.net.tokens(p)))
    case _ => None
  }).toList)

  def treeFold[A](z: A, f: (A, A) => A, l: List[A]): A = l match {
    case Nil => z
    case x :: Nil => f(z, x)
    case q @ (x :: xs) => treeFold(z, f, q.grouped(2).map({
      case List(a, b) => f(a, b)
      case List(a) => a
      case _ => throw new RuntimeException("Should not happen")
    }).toList)
  }

  def treeSequence[A](l: List[Expression[A]]): Expression[List[A]] =
    treeFold[Expression[List[A]]](constant(List()), (q, p) => (q <**> p)(_ ++ _), l.map(_.lwmap(List(_))))

  val imageV: Expression[(Node => Colorisation) => GraphicalContent] =
    CommonVisualSettings.settings >>= (settings =>
      (model.net.places.expr <***> (model.net.transitions, model.net.arcs))((p, t, a) =>
        (treeSequence(a.map(a => arcImage(a).map(i => (a, i.graphicalContent))) ++ (p ++ t).map(c => (componentImage(c, settings) <**> componentTransform(c))((img, xform) => (c, img.transform(xform).cgc)))))
          .map { list => (colorisation: (Node => Colorisation)) => treeFold[GraphicalContent](GraphicalContent.Empty, (_.compose(_)), (list.map { case (c, img) => img.applyColorisation(colorisation(c)) })) }).join)

  def image(colorisation: Node => Colorisation): Expression[GraphicalContent] = imageV map (_(colorisation))

  def componentImage(c: Component, settings: CommonVisualSettings): Expression[BoundedColorisableGraphicalContent] = c match {
    case p: Place => VisualPlace.image(model.net.tokens(p), model.net.label(p), settings)
    case t: Transition => VisualTransition.image(model.net.label(t), settings)
  }

  def arcImage(a: Arc): Expression[ConnectionGui] = for {
    visualArcs <- model.visualArcs;
    t1 <- touchable(a.from);
    t2 <- touchable(a.to);
    ap1 <- componentPosition(a.from);
    ap2 <- componentPosition(a.to)
  } yield {
    VisualConnectionGui.getConnectionGui(model.properties, VisualConnectionContext.makeContext(t1, ap1, t2, ap2), visualArcs(a))
  }

  def componentTransform(c: Component) = componentPosition(c).map(p => AffineTransform.getTranslateInstance(p.getX, p.getY))

  def componentPosition(c: Component): ModifiableExpression[Point2D.Double] =
    ModifiableExpression(model.layout.map(_(c)), p => model.layout.update(_ + (c -> p)))

  def nodePosition(n: Node): Option[ModifiableExpression[Point2D.Double]] = n match {
    case _: Arc => None
    case c: Component => Some(componentPosition(c))
  }

  def touchable(n: Node) = n match {
    case p: Place => (VisualPlace.touchable <**> componentTransform(p))((touchable, xform) => touchable.transform(xform))
    case t: Transition => (VisualTransition.touchable <**> componentTransform(t))((touchable, xform) => touchable.transform(xform))
    case a: Arc => arcImage(a).map(_.shape.touchable)
  }

  private def selectionTool = GenericSelectionTool[Node](
    model.net.nodes,
    model.selection,
    nodePosition(_),
    x => x,
    touchable(_),
    image(_),
    List())

  private val connectionManager = new ConnectionManager[Component] {
    def connect(node1: Component, node2: Component): Either[InvalidConnectionException, IO[Unit]] = (node1, node2) match {
      case (from: Place, to: Transition) => Right(model.createConsumerArc(from, to))
      case (from: Transition, to: Place) => Right(model.createProducerArc(from, to))
      case (_: Place, _: Place) => Left(new InvalidConnectionException("Arcs between places are invalid"))
      case (_: Transition, _: Transition) => Left(new InvalidConnectionException("Arcs between transitions are invalid"))
    }
  }

  private def connectionTool =
    GenericConnectionTool[Component](model.net.components, touchable(_), componentPosition(_), connectionManager, (f => image({
      case _: Arc => Colorisation.Empty
      case c: Component => f(c)
    })))

  private def placeGeneratorTool =
    NodeGeneratorTool(Button("Place", "images/icons/svg/place.svg", Some(KeyEvent.VK_P)).unsafePerformIO, image(_ => Colorisation(None, None)), model.createPlace(_))

  private def transitionGeneratorTool =
    NodeGeneratorTool(Button("Transition", "images/icons/svg/transition.svg", Some(KeyEvent.VK_T)).unsafePerformIO, image(_ => Colorisation(None, None)), model.createTransition(_))

  def tools = NonEmptyList(selectionTool, connectionTool, placeGeneratorTool, transitionGeneratorTool)
  def keyBindings = List(KeyBinding("Sumshit", KeyEvent.VK_Q, KeyEventType.KeyPressed, Set(), ioPure.pure { JOptionPane.showMessageDialog(null, "KUZUKA!", "Important message!", JOptionPane.INFORMATION_MESSAGE) }))
  def button = new Button {
    def hotkey = Some(KeyEvent.VK_K)
    def icon = None
    def label = "Hi :-)"
  }
}

class PetriNetModel extends Model {
 val properties = new VisualConnectionProperties {
    override def getDrawColor = Color.green
    override def getArrowWidth = 0.1
    override def getArrowLength = 0.2
    override def hasArrow = true
    override def getStroke = new BasicStroke(0.05f)
  }
  
  
  val net = new PetriNet
  val selection = Variable.create(Set[Node]())
  val layout = Variable.create(Map[Component, Point2D.Double]())
  val visualArcs = Variable.create(Map[Arc, StaticVisualConnectionData]())

  def createPlace(p: Point2D.Double): IO[Unit] = net.createPlace >>= (place => layout.update(_ + (place -> p)))
  def createTransition(p: Point2D.Double): IO[Unit] = net.createTransition >>= (transition => layout.update(_ + (transition -> p)))
  def createConsumerArc(from: Place, to: Transition) = net.createConsumerArc(from, to) >>= (a => visualArcs.update(_ + (a -> Polyline(List()))))
  def createProducerArc(from: Transition, to: Place) = net.createProducerArc(from, to) >>= (a => visualArcs.update(_ + (a -> Polyline(List()))))

  def implementation[T](service: Service[ModelScope, T]) = service match {
    case EditorService => Some(new PetriNetEditor(this))
    case _ => None
  }
}

/*
new ModelEditorTool {
    def interfacePanel = None
    def screenSpaceContent = Variable.create(GraphicalContent.Empty)
    def userSpaceContent = Variable.create(GraphicalContent.Empty)
    def mouseListener = Some(new DummyMouseListener {
      override def buttonClicked(button: MouseButton, clickCount: Int, modifiers: Set[Modifier], position: Point2D.Double) = ioPure.pure({
        println("Button clicked: " + " " + button + " " + position)
      })
      override def mouseMoved(modifiers: Set[Modifier], position: Point2D.Double): IO[Unit] = ioPure.pure({
        println("Moved: " + position)
      })

      override def dragStarted(button: MouseButton, position: Point2D.Double, modifiers: Set[Modifier]) = ioPure.pure({
        println("Drag started: " + " " + button + " " + position)
      })
      override def dragged(button: MouseButton, position: Point2D.Double, modifiers: Set[Modifier]) = ioPure.pure({
        println("Dragged: " + " " + button + " " + position)
      })
      override def dragFinished(button: MouseButton, position: Point2D.Double, modifiers: Set[Modifier]) = ioPure.pure({
        println("Drag finished: " + " " + button + " " + position)
      })

      override def buttonReleased(button: MouseButton, modifiers: Set[Modifier], position: Point2D.Double) = ioPure.pure({
        println("Button released: " + position)
      })

      override def buttonPressed(button: MouseButton, modifiers: Set[Modifier], position: Point2D.Double) = ioPure.pure({
        println("Button pressed: " + position)
      })
    })
*/