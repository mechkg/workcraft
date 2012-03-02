package org.workcraft.scala.grapheditor.tools
import org.workcraft.scala.Expressions._
import org.workcraft.scala.Util._
import org.workcraft.scala.Scalaz._
import java.awt.geom.Point2D
import org.workcraft.gui.graph.tools.selection.MoveDragHandler
import org.workcraft.gui.graph.tools.selection.GenericSelectionTool
import org.workcraft.gui.graph.Viewport
import org.workcraft.gui.graph.tools.AbstractTool
import org.workcraft.gui.graph.tools.Colorisation
import java.awt.Color
import org.workcraft.dependencymanager.advanced.core.Expressions
import org.workcraft.gui.graph.tools.GraphEditorMouseListener
import org.workcraft.graphics.Touchable
import org.workcraft.graphics.GraphicalContent

class SelectionTool[N](val mouseListener: GraphEditorMouseListener,
  val selectionBoxGraphics: (Viewport, Expression[Boolean]) => Expression[GraphicalContent],
  val effectiveSelection: Expression[Set[N]]) {
  def asGraphEditorTool ( paint: (Colorisation, Expression[Set[N]]) => Expression[GraphicalContent]) =
  {
    def graphics (viewport : Viewport, hasFocus: Expression[Boolean]) =
      (paint (SelectionTool.highlightedColorisation, effectiveSelection) <**> selectionBoxGraphics(viewport, hasFocus)) (_.compose(_))
      
    ToolHelper.asGraphEditorTool(Some(mouseListener), None, 
        Some(graphics), None, None, GenericSelectionTool.button)
  }
}

object SelectionTool {
  val highlightedColorisation = new Colorisation {
    override def getColorisation = new Color(99, 130, 191).brighter()
    override def getBackground = null
  }

  def create[N](
    nodes: Expression[Iterable[N]],
    selection: ModifiableExpression[Set[N]],
    movableController: N => Option[ModifiableExpression[Point2D.Double]],
    snap: Point2D.Double => Point2D.Double,
    touchableProvider: N => Expression[Touchable]
    ) = {
    val dragHandler = new MoveDragHandler[N](selection, movableController, snap)
    val genericSelectionTool = new GenericSelectionTool[N](selection.jexpr, HitTester.create(nodes, touchableProvider), dragHandler)

    new SelectionTool[N](genericSelectionTool.getMouseListener, (viewport, focus) => genericSelectionTool.userSpaceContent(viewport), genericSelectionTool.effectiveSelection)
  }
}
