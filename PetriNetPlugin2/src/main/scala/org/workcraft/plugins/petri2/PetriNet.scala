package org.workcraft.plugins.petri2
import org.workcraft.scala.Expressions.ModifiableExpression
import org.workcraft.dependencymanager.advanced.user.Variable
import org.workcraft.services.Model
import org.workcraft.services.Service
import org.workcraft.services.ModelScope
import org.workcraft.gui.modeleditor.EditorService
import org.workcraft.gui.modeleditor.ModelEditor
import scalaz.NonEmptyList
import org.workcraft.gui.modeleditor.tools.ModelEditorTool
import org.workcraft.graphics.GraphicalContent
import org.workcraft.gui.modeleditor.tools.Button

sealed trait Node

class Component(val label: Variable[String]) extends Node
class Place(val tokens: Variable[Int], label: Variable[String]) extends Component(label)
class Transition(label: Variable[String]) extends Component(label)

class Arc extends Node
class ProducerArc(val from: Transition, val to: Place)
class ConsumerArc(val from: Place, val to: Transition)

class PetriNet {
  val places = Variable.create(List[Place]())
  val transitions = Variable.create(List[Transition]())
  val arcs = Variable.create(List[Arc]())
}

class PetriNetEditor(net: PetriNet) extends ModelEditor {
  def tools = NonEmptyList(new ModelEditorTool {
    def interfacePanel = None
    def screenSpaceContent = None
    def userSpaceContent = Variable.create(GraphicalContent.Empty)
    def mouseListener = {}
    def keyBindings = None
    def button = new Button {
      def hotkey = None
      def icon = None
      def label = "Hi :-)"
    }
  },new ModelEditorTool {
    def interfacePanel = None
    def screenSpaceContent = None
    def userSpaceContent = Variable.create(GraphicalContent.Empty)
    def mouseListener = {}
    def keyBindings = None
    def button = new Button {
      def hotkey = None
      def icon = None
      def label = "Hi :-)"
    }
  },new ModelEditorTool {
    def interfacePanel = None
    def screenSpaceContent = None
    def userSpaceContent = Variable.create(GraphicalContent.Empty)
    def mouseListener = {}
    def keyBindings = None
    def button = new Button {
      def hotkey = None
      def icon = None
      def label = "Hi :-)"
    }
  },new ModelEditorTool {
    def interfacePanel = None
    def screenSpaceContent = None
    def userSpaceContent = Variable.create(GraphicalContent.Empty)
    def mouseListener = {}
    def keyBindings = None
    def button = new Button {
      def hotkey = None
      def icon = None
      def label = "Hi :-)"
    }
  },new ModelEditorTool {
    def interfacePanel = None
    def screenSpaceContent = None
    def userSpaceContent = Variable.create(GraphicalContent.Empty)
    def mouseListener = {}
    def keyBindings = None
    def button = new Button {
      def hotkey = None
      def icon = None
      def label = "Hi :-)"
    }
  })
}

class PetriNetModel extends Model {
  val net = new PetriNet

  def implementation[T](service: Service[ModelScope, T]) = service match {
    case EditorService => Some(new PetriNetEditor(net))
    case _ => None
  }
}