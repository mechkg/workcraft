package org.workcraft.scala.grapheditor.tools

import org.workcraft.scala.Util._
import org.workcraft.scala.Scalaz._
import org.workcraft.scala.effects.IO._
import org.workcraft.scala.Expressions._
import java.awt.geom.Point2D
import java.awt.Color
import org.workcraft.graphics.Colorisation
import org.workcraft.graphics.Touchable
import org.workcraft.graphics.GraphicalContent
import org.workcraft.gui.modeleditor.tools.ConnectionManager
import org.workcraft.gui.modeleditor.tools.ModelEditorTool
import org.workcraft.gui.modeleditor.tools.Button
import java.awt.event.KeyEvent
import org.workcraft.gui.GUI
import org.workcraft.gui.modeleditor.tools.GenericConnectionToolImpl
import org.workcraft.gui.modeleditor.Viewport
import org.workcraft.gui.modeleditor.tools.ToolEnvironment
import org.workcraft.gui.modeleditor.tools.ModelEditorToolInstance

class GenericConnectionToolInstance[N](
  viewport: Viewport,
  hasFocus: Expression[Boolean],
  components: Expression[Iterable[N]],
  touchableProvider: N => Expression[Touchable],
  arrowPoint: N => Expression[Point2D.Double],
  connectionController: ConnectionManager[N],
  paint: (N => Colorisation) => Expression[GraphicalContent]) extends ModelEditorToolInstance {

  import GenericConnectionTool._

  val impl = new GenericConnectionToolImpl(arrowPoint, connectionController, n => ioPure.pure { HitTester.create(components, touchableProvider).hitTest(n) })

  def keyBindings = List()
  def mouseListener = Some(impl.mouseListener)
  def userSpaceContent = impl.mouseOverNode >>= (mo =>
    (paint(n => if (Some(n) == mo) highlightedColorisation else Colorisation.Empty) <**>
      impl.connectingLineGraphicalContent(viewport))(_.compose(_)))

  def screenSpaceContent = impl.screenSpaceContent(viewport, hasFocus)
  def interfacePanel = None
}

case class GenericConnectionTool[N](components: Expression[Iterable[N]],
  touchableProvider: N => Expression[Touchable],
  arrowPoint: N => Expression[Point2D.Double],
  connectionController: ConnectionManager[N],
  paint: (N => Colorisation) => Expression[GraphicalContent]) extends ModelEditorTool {

  def button = GenericConnectionTool.button
  def createInstance(env: ToolEnvironment) = ioPure.pure { new GenericConnectionToolInstance(env.viewport, env.hasFocus, components, touchableProvider, arrowPoint, connectionController, paint) }

}

object GenericConnectionTool {
  val highlightedColorisation = Colorisation(Some(new Color(99, 130, 191).brighter), None)
  val button =
    new Button {
      override def hotkey = Some(KeyEvent.VK_C)
      override def icon = Some(GUI.createIconFromSvgUsingSettingsSize("images/icons/svg/connect.svg").unsafePerformIO)
      override def label = "Connection tool"
    }
}