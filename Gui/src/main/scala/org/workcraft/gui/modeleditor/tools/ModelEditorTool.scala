package org.workcraft.gui.modeleditor.tools
import javax.swing.Icon
import org.workcraft.scala.Expressions.Expression
import org.workcraft.graphics.GraphicalContent
import javax.swing.JPanel
import org.workcraft.gui.GUI
import org.workcraft.scala.effects.IO
import org.workcraft.gui.modeleditor.KeyBinding
import org.workcraft.gui.modeleditor.ToolMouseListener
import org.workcraft.gui.modeleditor.Viewport

import org.workcraft.scala.Expressions._

trait Button {
  def label: String
  def icon: Option[Icon]
  def hotkey: Option[Int]
}

object Button {
  def apply(_label: String, iconPath: String, _hotkey: Option[Int]): IO[Button] = {
    GUI.createIconFromSvgUsingSettingsSize(iconPath).map(ico => new Button {
      val label = _label
      val icon = Some(ico)
      val hotkey = _hotkey
    })
  }
}

trait ModelEditorTool extends ModelEditorTool2Activation {
  def button: Button
}

case class ToolEnvironment(viewport: Viewport, hasFocus: Expression[Boolean])

object ModelEditorTool {
  type ModelEditorToolConstructor = ToolEnvironment => ModelEditorTool
  implicit def as2(c : ModelEditorToolConstructor) : ModelEditorTool2 = new ModelEditorTool2 {
    val button = c(ToolEnvironment(new Viewport(constant((0,0,10,10))), constant(false))).button
    def activate (env : ToolEnvironment) : IO[ModelEditorTool2Activation] = IO.ioPure.pure {
      val me1 = c(env)
      new ModelEditorTool2Activation{
        val keyBindings = me1.keyBindings
        val mouseListener = me1.mouseListener
        val userSpaceContent = me1.userSpaceContent
        val screenSpaceContent = me1.screenSpaceContent
        val interfacePanel = me1.interfacePanel
      }
    }
  }
}

trait ModelEditorTool2Activation {
  def keyBindings: List[KeyBinding]
  def mouseListener: Option[ToolMouseListener]
  def userSpaceContent: Expression[GraphicalContent]
  def screenSpaceContent: Expression[GraphicalContent]
  def interfacePanel: Option[JPanel]
}

trait ModelEditorTool2 {
  def button: Button
  def activate(env : ToolEnvironment) : IO[ModelEditorTool2Activation]
}
