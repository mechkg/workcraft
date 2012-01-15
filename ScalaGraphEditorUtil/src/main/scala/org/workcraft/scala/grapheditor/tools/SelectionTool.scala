package org.workcraft.scala.grapheditor.tools
import org.workcraft.dependencymanager.advanced.user.{ModifiableExpression => JModifiableExpression}
import pcollections.PSet
import org.workcraft.scala.Expressions._
import org.workcraft.scala.Util._
import org.workcraft.scala.Scalaz._
import org.workcraft.graphics.Graphics._
import org.workcraft.dependencymanager.advanced.core.{Expression => JExpression}
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
      (paint (SelectionTool.highlightedColorisation, effectiveSelection) <**> selectionBoxGraphics(viewport, hasFocus)) (compose(_,_))
      
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
    selection: ModifiableExpression[PSet[N]],
    movableController: N => Maybe[ModifiableExpression[Point2D.Double]],
    snap: Point2D.Double => Point2D.Double,
    touchableProvider: N => Expression[Touchable]
    ) = {
    val dragHandler = new MoveDragHandler[N](selection.jexpr, ((m : Maybe[ModifiableExpression[Point2D.Double]]) => asMaybe(for (m <- m) yield m.jexpr)) compose movableController, snap)
    val genericSelectionTool = new GenericSelectionTool[N](selection.jexpr, HitTester.create(nodes, touchableProvider), dragHandler)

    new SelectionTool[N](genericSelectionTool.getMouseListener, (viewport, focus) => genericSelectionTool.userSpaceContent(viewport), genericSelectionTool.effectiveSelection)
  }
}
