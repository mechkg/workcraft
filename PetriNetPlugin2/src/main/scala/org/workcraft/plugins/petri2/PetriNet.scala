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
case class Place extends Component
case class Transition extends Component

sealed trait Arc extends Node
case class ProducerArc(from: Transition, to: Place) extends Arc
case class ConsumerArc(from: Place, to: Transition) extends Arc

class PetriNet {
  private val marking_ = Map[Place, ModifiableExpression[Int]]()
  private val labelling_ = Map[Component, ModifiableExpression[String]]()

  private val places_ = Variable.create(List[Place]())
  private val transitions_ = Variable.create(List[Transition]())
  private val arcs_ = Variable.create(List[Arc]())

  val nodes: Expression[List[Node]] = for {
    p <- places_;
    t <- transitions_;
    a <- arcs_
  } yield p ++ t ++ a

  val places: Expression[List[Place]] = places_
  val transitions: Expression[List[Transition]] = transitions_
  val arcs: Expression[List[Arc]] = arcs_

  def tokens(place: Place): Expression[Int] = marking_(place)
  def label(c: Component): Expression[String] = labelling_(c)

  def setTokens(place: Place, n: Int) = set(marking_(place), n)
}

class PetriNetEditor(model: PetriNetModel) extends ModelEditor {

  def image(colorisation: Node => Colorisation): Expression[GraphicalContent] =
  (model.net.places <**> model.net.transitions)((p, t) =>
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
    position(_: Node),
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