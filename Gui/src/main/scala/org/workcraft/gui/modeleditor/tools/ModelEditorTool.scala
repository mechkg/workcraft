package org.workcraft.gui.modeleditor.tools
import javax.swing.Icon
import org.workcraft.scala.Expressions.Expression
import org.workcraft.graphics.GraphicalContent
import javax.swing.JPanel

trait Button {
  def label: String
  def icon: Option[Icon]
  def hotkey: Option[Int]
}

trait ModelEditorTool {
  def button : Button
  def keyListener
  def mouseListener
  def userSpaceContent: Expression[GraphicalContent]
  def screenSpaceContent: Expression[GraphicalContent]
  def interfacePanel: Option[JPanel]
}