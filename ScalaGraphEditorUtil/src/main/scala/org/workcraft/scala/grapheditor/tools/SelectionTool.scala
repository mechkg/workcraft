package org.workcraft.scala.grapheditor.tools
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression
import pcollections.PSet
import org.workcraft.scala.Util._
import org.workcraft.graphics.Graphics._
import org.workcraft.dependencymanager.advanced.core.Expression
import java.awt.geom.Point2D
import org.workcraft.dom.visual.Touchable
import org.workcraft.dom.visual.ColorisableGraphicalContent
import org.workcraft.gui.graph.tools.selection.MoveDragHandler
import org.workcraft.gui.graph.tools.selection.GenericSelectionTool
import org.workcraft.gui.graph.Viewport
import org.workcraft.gui.graph.tools.AbstractTool
import org.workcraft.gui.graph.tools.Colorisation
import java.awt.Color
import org.workcraft.dependencymanager.advanced.core.Expressions
import org.workcraft.dom.visual.GraphicalContent
import org.workcraft.gui.graph.tools.GraphEditorMouseListener
import org.workcraft.util.Maybe

class SelectionTool[N](val mouseListener: GraphEditorMouseListener,
  val selectionBoxGraphics: (Viewport, Expression[java.lang.Boolean]) => Expression[GraphicalContent],
  val effectiveSelection: Expression[PSet[N]]) {
  def asGraphEditorTool ( paint: (Colorisation, Expression[_ <: java.util.Set[N]]) => Expression[GraphicalContent]) =
  {
    def graphics (viewport : Viewport, hasFocus: Expression[java.lang.Boolean]) =
      compose (paint (SelectionTool.highlightedColorisation, effectiveSelection), selectionBoxGraphics(viewport, hasFocus))
      
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
    nodes: Expression[_ <: Iterable[N]],
    selection: ModifiableExpression[PSet[N]],
    movableController: N => Maybe[ModifiableExpression[Point2D]],
    snap: Point2D => Point2D,
    touchableProvider: N => Expression[Touchable]
    ) = {
    val dragHandler = new MoveDragHandler[N](selection, movableController, snap)
    val genericSelectionTool = new GenericSelectionTool[N](selection, HitTester.create(nodes, touchableProvider), dragHandler)

    new SelectionTool[N](genericSelectionTool.getMouseListener, (viewport, focus) => genericSelectionTool.userSpaceContent(viewport), genericSelectionTool.effectiveSelection)
  }
}