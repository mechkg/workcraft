package org.workcraft.gui.modeleditor
import javax.swing.Icon
import org.workcraft.scala.Expressions.Expression
import org.workcraft.graphics.GraphicalContent
import javax.swing.JPanel

package object tools {
  trait Button {
    def label: String
    def icon: Option[Icon]
    def hotkey: Option[Int]
  }

  trait ModelEditorTool {
    def button: Button
    def keyBindings: List[KeyBinding]
    def mouseListener: Option[ToolMouseListener]
    def userSpaceContent: Expression[GraphicalContent]
    def screenSpaceContent: Expression[GraphicalContent]
    def interfacePanel: Option[JPanel]
  }
  
  trait ToolEnvironment {
    def viewport: Viewport
    def hasFocus: Expression[Boolean]
  }

  type ModelEditorTool8 = ToolEnvironment => ModelEditorTool
}