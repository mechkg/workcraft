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

sealed trait Node

sealed trait Component extends Node
case class Place private[petri2] extends Component
case class Transition private[petri2] extends Component

sealed trait Arc extends Node
case class ProducerArc private[petri2] (from: Transition, to: Place) extends Arc
case class ConsumerArc private[petri2] (from: Place, to: Transition) extends Arc

class PetriNet {
  val marking = Map[Place, ModifiableExpression[Int]]()
  val labelling = Map[Component, ModifiableExpression[String]]()
  val places = Variable.create(List[Place]())
  val transitions = Variable.create(List[Transition]())
  val arcs = Variable.create(List[Arc]())

  val nodes: Expression[List[Node]] = for {
    p <- places;
    t <- transitions;
    a <- arcs
  } yield p ++ t ++ a

 
  def tokens(place: Place) = marking(place)
  def label(c: Component) = labelling(c)
}

class PetriNetEditor(model: PetriNetModel) extends ModelEditor {
  def image(colorisation: Node => Colorisation): Expression[GraphicalContent] =
  (model.net.places.expr <**> model.net.transitions.expr)((p, t) =>
    (p++t).map(c => ((componentImage(c)) <**> componentTransform(c))
        ((img, xform) => img.transform(xform).cgc.applyColorisation(colorisation(c))))
      .sequence.map(_.foldLeft(GraphicalContent.Empty)(_.compose(_)))).join
          
  def componentImage (c: Component) = c match {
    case p:Place => VisualPlace.image(model.net.tokens(p), model.net.label(p))
    case t:Transition => VisualTransition.image(model.net.label(t)) 
  }       
   
  def componentTransform(c: Component) = componentPosition(c).map(p => AffineTransform.getTranslateInstance(p.getX, p.getY))

  def componentPosition(c: Component) = model.layout(c)

  def nodePosition(n: Node) = n match {
    case _: Arc => None
    case c: Component => componentPosition(c)
  }

  def touchable(n: Node) = n match {
    case _: Place => VisualPlace.touchable
    case _: Transition => VisualTransition.touchable
    case _: Arc => null
  }

  private def selectionTool = GenericSelectionTool(
    model.net.nodes,
    model.selection,
    null,
    x => x,
    null,
    null,
    null)

  def tools = null
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
  val layout = Map[Component, ModifiableExpression[Point2D.Double]]()

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