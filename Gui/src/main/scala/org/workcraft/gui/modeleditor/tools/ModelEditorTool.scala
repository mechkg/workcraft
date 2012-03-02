package org.workcraft.gui.modeleditor.tools
import javax.swing.Icon
import org.workcraft.scala.Expressions.Expression
import org.workcraft.graphics.GraphicalContent
import javax.swing.JPanel
import org.workcraft.gui.modeleditor.KeyBinding

trait Button {
  def label: String
  def icon: Option[Icon]
  def hotkey: Option[Int]
}

trait ModelEditorTool {
  def button: Button
  def keyBindings: Option[List[KeyBinding]]
  def mouseListener
  def userSpaceContent: Expression[GraphicalContent]
  def screenSpaceContent: Option[Expression[GraphicalContent]]
  def interfacePanel: Option[JPanel]
}