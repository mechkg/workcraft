package org.workcraft.scala.grapheditor.tools
import org.workcraft.scala.Expressions._
import org.workcraft.scala.Util._
import org.workcraft.scala.Scalaz._
import java.awt.geom.Point2D
import org.workcraft.gui.modeleditor.tools.selection.MoveDragHandler
import org.workcraft.gui.modeleditor.Viewport
import java.awt.Color
import org.workcraft.dependencymanager.advanced.core.Expressions
import org.workcraft.graphics.Touchable
import org.workcraft.graphics.GraphicalContent
import org.workcraft.gui.modeleditor.tools._
import org.workcraft.gui.modeleditor.KeyBinding
import org.workcraft.gui.modeleditor.tools.Button
import java.awt.event.KeyEvent
import org.workcraft.gui.GUI
import org.workcraft.graphics.Colorisation
import org.workcraft.gui.modeleditor.tools.selection.GenericSelectionToolMouseListener

import org.workcraft.scala.effects.IO
import org.workcraft.scala.effects.IO._
import org.workcraft.dependencymanager.advanced.user.Variable

import scalaz._
import Scalaz._

import org.workcraft.scala.Expressions._

class GenericSelectionToolInstance[N](
  viewport: Viewport,
  nodes: Expression[Iterable[N]],
  selection: ModifiableExpression[Set[N]],
  moveOperation: (Set[N], Point2D.Double) => IO[Unit],
  offsetSnap: (N, Point2D.Double) => Point2D.Double,
  touchable: N => Expression[Touchable],
  paint: (N => Colorisation, Set[N], Point2D.Double) => Expression[GraphicalContent],
  customKeyBindings: List[KeyBinding],
  doubleClickHandler: Option[N => IO[Unit]]) extends ModelEditorToolInstance {

  private val currentOffset = Variable.create(new Point2D.Double(0, 0))

  private val mouseListener_ = new GenericSelectionToolMouseListener(selection, HitTester.create(nodes, touchable),
    new MoveDragHandler(currentOffset, offsetSnap, (selection.expr <**> currentOffset)(moveOperation(_, _)).eval.join), doubleClickHandler)

  def keyBindings = customKeyBindings
  def mouseListener = Some(mouseListener_)
  val userSpaceContent = {
    val modelImage = (mouseListener_.effectiveSelection <**> (currentOffset))((selection, offset) =>
      paint(n => if (selection contains n) GenericSelectionTool.highlightedColorisation else Colorisation.Empty, selection, offset)).join

    val selectionBoxImage = mouseListener_.userSpaceContent(viewport)

    (modelImage <**> selectionBoxImage)(_.compose(_))
  }

  def screenSpaceContent = constant(GraphicalContent.Empty)
  def interfacePanel = None
}

case class GenericSelectionTool[N] (
  nodes: Expression[Iterable[N]],
  selection: ModifiableExpression[Set[N]],
  moveOperation: (Set[N], Point2D.Double) => IO[Unit],
  offsetSnap: (N, Point2D.Double) => Point2D.Double,
  touchable: N => Expression[Touchable],
  paint: (N => Colorisation, Set[N], Point2D.Double) => Expression[GraphicalContent],
  customKeyBindings: List[KeyBinding],
  doubleClickHandler: Option[N => IO[Unit]]) extends ModelEditorTool {
  
  def button = GenericSelectionTool.button
  def createInstance (env: ToolEnvironment) = ioPure.pure { new GenericSelectionToolInstance (env.viewport, nodes, selection, moveOperation, offsetSnap, touchable, paint, customKeyBindings, doubleClickHandler) }
}

object GenericSelectionTool {
  val button = new Button {
    override def label = "Selection tool"
    override def hotkey = Some(KeyEvent.VK_S)
    override def icon = Some(GUI.createIconFromSvgUsingSettingsSize("images/icons/svg/select.svg").unsafePerformIO)
  }

  val highlightedColorisation = Colorisation(Some(new Color(99, 130, 191).brighter), None)
}
