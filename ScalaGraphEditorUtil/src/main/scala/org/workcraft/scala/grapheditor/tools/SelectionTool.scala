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



class GenericSelectionTool[N] (
  viewport: Viewport,
  nodes: Expression[Iterable[N]],
  selection: ModifiableExpression[Set[N]],
  position: N => Option[ModifiableExpression[Point2D.Double]],
  snap: Point2D.Double => Point2D.Double,
  touchable: N => Expression[Touchable],
  paint: (N => Colorisation) => Expression[GraphicalContent],
  customKeyBindings: List[KeyBinding]) extends ModelEditorTool {
  
  private val mouseListener_ = new GenericSelectionToolMouseListener(selection, HitTester.create(nodes, touchable), new MoveDragHandler(selection, position, snap))
  
  def button = GenericSelectionTool.button

  def keyBindings = customKeyBindings
  def mouseListener = Some(mouseListener_)
  def userSpaceContent = mouseListener_.effectiveSelection >>=
    (selection => (paint (n => if(selection contains n) GenericSelectionTool.highlightedColorisation else Colorisation.Empty) |@| 
    mouseListener_.userSpaceContent(viewport) ) (_.compose(_)))
  def screenSpaceContent = constant(GraphicalContent.Empty)
  def interfacePanel = None
}

object GenericSelectionTool {
    val button = new Button {
    override def label = "Selection tool"
    override def hotkey = Some(KeyEvent.VK_S)
    override def icon = Some(GUI.createIconFromSvgUsingSettingsSize("images/icons/svg/select.svg").unsafePerformIO)
  }

  val highlightedColorisation = Colorisation (Some(new Color(99, 130, 191).brighter), None)
  def apply[N](
  nodes: Expression[Iterable[N]],
  selection: ModifiableExpression[Set[N]],
  position: N => Option[ModifiableExpression[Point2D.Double]],
  snap: Point2D.Double => Point2D.Double,
  touchable: N => Expression[Touchable],
  paint: (N => Colorisation) => Expression[GraphicalContent],
  customKeyBindings: List[KeyBinding]) : ModelEditorToolMaker = 
    env => new GenericSelectionTool(
          env.viewport,
          nodes,
          selection,
          position,
          snap,
          touchable,
          paint,
          customKeyBindings
    )
}
