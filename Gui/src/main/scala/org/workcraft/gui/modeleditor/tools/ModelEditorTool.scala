package org.workcraft.gui.modeleditor
import javax.swing.Icon
import org.workcraft.scala.Expressions.Expression
import org.workcraft.graphics.GraphicalContent
import javax.swing.JPanel
import org.workcraft.gui.GUI
import org.workcraft.scala.effects.IO

package object tools {
  trait Button {
    def label: String
    def icon: Option[Icon]
    def hotkey: Option[Int]
  }
  
  object Button {
    def create(_label : String, iconPath : String, _hotkey : Option[Int]) : IO [Button] = {
      GUI.createIconFromSvgUsingSettingsSize(iconPath).map(ico => new Button {
        val label = _label
        val icon = Some(ico)
        val hotkey = _hotkey
      })
    }
  }

  trait ModelEditorTool {
    def button: Button
    def keyBindings: List[KeyBinding]
    def mouseListener: Option[ToolMouseListener]
    def userSpaceContent: Expression[GraphicalContent]
    def screenSpaceContent: Expression[GraphicalContent]
    def interfacePanel: Option[JPanel]
  }
  
  case class ToolEnvironment(viewport: Viewport, hasFocus: Expression[Boolean])

  type ModelEditorToolMaker = ToolEnvironment => ModelEditorTool
}
