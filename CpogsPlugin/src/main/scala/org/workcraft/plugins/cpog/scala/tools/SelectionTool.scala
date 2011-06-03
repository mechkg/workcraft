package org.workcraft.plugins.cpog.scala.tools
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression
import pcollections.PSet
import org.workcraft.plugins.cpog.scala.nodes._
import org.workcraft.plugins.cpog.scala.Util._
import org.workcraft.plugins.cpog.scala.Graphics._
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

object SelectionTool {
  val highlightedColorisation = new Colorisation {
    override def getColorisation = new Color(99, 130, 191).brighter()
    override def getBackground = null
  }

  def create(
    selection: ModifiableExpression[PSet[Node]],
    nodes: Expression[_ <: Iterable[Node]],
    snap: Point2D => Point2D,
    touchable: Node => Expression[Touchable],
    painter: Node => Expression[ColorisableGraphicalContent]) = {
    val dragHandler = new MoveDragHandler[Node](selection, MovableController.position(_: Node), snap)

    val genericSelectionTool = new GenericSelectionTool[Node](selection, HitTester.create(nodes, touchable), dragHandler)

    new AbstractTool {
      override def mouseListener = genericSelectionTool.getMouseListener
      override def userSpaceContent(viewport: Viewport, hasFocus: Expression[Boolean]) =
        compose(
          paint(colouriseWithHighlights[Node](highlightedColorisation, genericSelectionTool.effectiveSelection(), painter), nodes),
          genericSelectionTool.userSpaceContent(viewport))
      override def screenSpaceContent(viewport: Viewport, hasFocus: Expression[Boolean]) = Expressions.constant(GraphicalContent.EMPTY)
      override def getButton = GenericSelectionTool.button
    }
  }
}