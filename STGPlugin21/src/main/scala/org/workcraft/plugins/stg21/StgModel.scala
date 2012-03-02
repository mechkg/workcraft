package org.workcraft.plugins.stg21
import org.workcraft.services.ModelServiceProvider
import org.workcraft.services.Service
import org.workcraft.services.ModelScope
import org.workcraft.gui.modeleditor.EditorService

class StgModel extends ModelServiceProvider {
  def implementation[T](service: Service[ModelScope, T]) = service match {
    case EditorService => Some(new PetriNetEditor(net))
    case _ => None
  }
}
/*
class PetriNetEditor(net: PetriNet) extends ModelEditor {
  def tools = NonEmptyList(new ModelEditorTool {
    def interfacePanel = None
    def screenSpaceContent = Variable.create(GraphicalContent.Empty)
    def userSpaceContent = Variable.create(GraphicalContent.Empty)
    def mouseListener = Some(new DummyMouseListener {
      override def mousePressed(button: MouseButton, modifiers: Set[Modifier], position: Point2D.Double) = ioPure.pure ({
        if (modifiers.contains(Control))
        	println("Heee hee heee " + position)
        else
          println(position)
      })
    })
    def keyBindings = List(KeyBinding("Sumshit", KeyEvent.VK_Q, KeyPressed, Set(),  ioPure.pure {JOptionPane.showMessageDialog (null, "KUZUKA!", "Important message!", JOptionPane.INFORMATION_MESSAGE)} ))
    def button = new Button {
      def hotkey = Some(KeyEvent.VK_K)
      def icon = None
      def label = "Hi :-)"
    }
  })
}
*/