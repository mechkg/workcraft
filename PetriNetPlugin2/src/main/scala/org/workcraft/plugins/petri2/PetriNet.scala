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

sealed trait Node

sealed trait Component extends Node
class Place private[petri2] extends Component
class Transition private[petri2] extends Component

sealed trait Arc extends Node
case class ProducerArc private[petri2] (from: Transition, to: Place) extends Arc
case class ConsumerArc private[petri2] (from: Place, to: Transition) extends Arc

class PetriNet {
  val marking = Variable.create(Map[Place, Int]())
  val labelling = Variable.create(Map[Component, String]())
  val places = Variable.create(List[Place]())
  val transitions = Variable.create(List[Transition]())
  val arcs = Variable.create(List[Arc]())

  val nodes: Expression[List[Node]] = for {
    p <- places;
    t <- transitions;
    a <- arcs
  } yield p ++ t ++ a

 
  def tokens(place: Place) = marking.map(_(place))
  def label(c: Component) = labelling.map(_(c))
  
  private def newPlace = ioPure.pure{ new Place }
  private def newTransition = ioPure.pure {new Transition}
  
  def createPlace: IO[Place] = for {
    p <- newPlace;
    _ <- places.update(p :: _);
    _ <- marking.update (_ + (p-> 0));
    _ <- labelling.update (_ + (p -> "Pitsot"))
  } yield p 
  
  def createTransition: IO[Transition] = for {
    t <- newTransition;
    _ <- transitions.update(t::_);
    _ <- labelling.update(_ + (t -> "Sto"))
  } yield t
}

class PetriNetEditor(model: PetriNetModel) extends ModelEditor {
  
  val imageV: Expression[(Node => Colorisation) => GraphicalContent] =
  (model.net.places.expr <**> model.net.transitions.expr)((p, t) =>
    (p++t).traverse
      (c => (componentImage(c) <**> componentTransform(c)) ((img, xform) => (c, img.transform(xform).cgc)))
      .map{list => (colorisation : (Node => Colorisation)) => (list.map {case (c, img) => img.applyColorisation(colorisation(c))}.foldLeft(GraphicalContent.Empty)(_.compose(_)))}
      ).join
  
  def image(colorisation: Node => Colorisation): Expression[GraphicalContent] = imageV map (_(colorisation))

  def componentImage (c: Component) = c match {
    case p:Place => VisualPlace.image(model.net.tokens(p), model.net.label(p))
    case t:Transition => VisualTransition.image(model.net.label(t)) 
  }       
   
  def componentTransform(c: Component) = componentPosition(c).map(p => AffineTransform.getTranslateInstance(p.getX, p.getY))

  def componentPosition(c: Component): ModifiableExpression[Point2D.Double] =
    ModifiableExpression (model.layout.map(_(c)), p => model.layout.update(_ + (c -> p)))

  def nodePosition(n: Node): Option[ModifiableExpression[Point2D.Double]] = n match {
    case _: Arc => None
    case c: Component => Some(componentPosition(c))
  }

  def touchable(n: Node) = n match {
    case p: Place => (VisualPlace.touchable <**> componentTransform(p)) ((touchable, xform) => touchable.transform(xform))
    case t: Transition => (VisualTransition.touchable <**> componentTransform(t)) ((touchable, xform) => touchable.transform(xform))
    case _: Arc => null
  }

  private def selectionTool = GenericSelectionTool[Node](
    model.net.nodes,
    model.selection,
    nodePosition(_),
    x => x,
    touchable(_),
    image(_),
    List())
    
  private def placeGeneratorTool = 
    NodeGeneratorTool(Button("Place", "images/icons/svg/place.svg", Some(KeyEvent.VK_P)).unsafePerformIO, image(_ => Colorisation(None, None)), model.createPlace(_))
  
  private def transitionGeneratorTool =
    NodeGeneratorTool(Button("Transition", "images/icons/svg/transition.svg", Some(KeyEvent.VK_T)).unsafePerformIO, image(_ => Colorisation(None, None)), model.createTransition(_))

  def tools = NonEmptyList(selectionTool, placeGeneratorTool, transitionGeneratorTool)
  def keyBindings = List(KeyBinding("Sumshit", KeyEvent.VK_Q, KeyEventType.KeyPressed, Set(), ioPure.pure { JOptionPane.showMessageDialog(null, "KUZUKA!", "Important message!", JOptionPane.INFORMATION_MESSAGE) }))
  def button = new Button {
    def hotkey = Some(KeyEvent.VK_K)
    def icon = None
    def label = "Hi :-)"
  }
}

class PetriNetModel extends Model {
  val net = new PetriNet
  val selection = Variable.create(Set[Node]())
  val layout = Variable.create(Map[Component, Point2D.Double]())
  
  def createPlace(p: Point2D.Double): IO[Unit] = net.createPlace >>= (place => layout.update(_ + (place -> p)))
  def createTransition(p: Point2D.Double): IO[Unit] = net.createTransition >>= (transition => layout.update(_ + (transition -> p)))
  

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