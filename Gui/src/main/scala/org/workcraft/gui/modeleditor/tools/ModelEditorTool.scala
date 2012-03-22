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
import scalaz._
import Scalaz._

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

case class ToolEnvironment(viewport: Viewport, hasFocus: Expression[Boolean])

class ModelEditorToolInstanceCopy(me1: ModelEditorToolInstance) extends ModelEditorToolInstance {
  val keyBindings = me1.keyBindings
  val mouseListener = me1.mouseListener
  val userSpaceContent = me1.userSpaceContent
  val screenSpaceContent = me1.screenSpaceContent
  val interfacePanel = me1.interfacePanel
}

class EmptyModelEditorToolInstance extends ModelEditorToolInstance {
  def keyBindings = List()
  def mouseListener : Option[ToolMouseListener] = None
  def userSpaceContent = constant(GraphicalContent.Empty)
  def screenSpaceContent = constant(GraphicalContent.Empty)
  def interfacePanel : Option[JPanel] = None
}

trait ModelEditorToolInstance {
  def keyBindings: List[KeyBinding]
  def mouseListener: Option[ToolMouseListener]
  def userSpaceContent: Expression[GraphicalContent]
  def screenSpaceContent: Expression[GraphicalContent]
  def interfacePanel: Option[JPanel]
}

object ModelEditorToolInstance {
  implicit def applicativeSemigroup[A : Semigroup, F[_] : Applicative] : Semigroup[F[A]]= semigroup ((p,q) => {
    (p <**> q) (_ |+| _)
  })
  
  implicit def met2aSemigroup : Semigroup[ModelEditorToolInstance] = semigroup((a,b) => new ModelEditorToolInstance {
    val keyBindings = a.keyBindings |+| b.keyBindings
    val mouseListener : Option[ToolMouseListener] = LastOptionTo(a.mouseListener) |+| LastOptionTo(b.mouseListener)
    val userSpaceContent = a.userSpaceContent |+| b.userSpaceContent
    val screenSpaceContent = a.screenSpaceContent |+| b.screenSpaceContent
    val interfacePanel : Option[JPanel] = LastOptionTo(a.interfacePanel) |+| LastOptionTo(b.interfacePanel)
  })
}

trait ModelEditorTool {
  def button: Button
  def createInstance(env : ToolEnvironment) : IO[ModelEditorToolInstance]
}
