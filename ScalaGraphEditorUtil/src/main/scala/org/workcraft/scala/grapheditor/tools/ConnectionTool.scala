package org.workcraft.scala.grapheditor.tools

import org.workcraft.gui.graph.tools.GenericConnectionTool
import org.workcraft.scala.Util._
import org.workcraft.scala.Scalaz._
import org.workcraft.scala.Expressions._
import org.workcraft.gui.graph.tools.ConnectionController
import java.awt.geom.Point2D
import org.workcraft.gui.graph.tools.AbstractTool
import org.workcraft.gui.graph.Viewport
import org.workcraft.gui.graph.tools.Colorisation
import java.awt.Color
import org.workcraft.gui.graph.tools.GraphEditorMouseListener
import org.workcraft.gui.graph.tools.GraphEditorKeyListener
import org.workcraft.graphics.(Colorisation,Expression;
import org.workcraft.graphics.Touchable
import org.workcraft.graphics.GraphicalContent
import org.workcraft.gui.graph.tools.ConnectionManager

class ConnectionTool[N](val mouseListener: GraphEditorMouseListener,
  val connectingLineGraphics: (Viewport, Expression[Boolean]) => Expression[GraphicalContent],
  val hintGraphics: (Viewport, Expression[Boolean]) => Expression[GraphicalContent],
  val mouseOver: Expression[Option[_ <: N]]) {
  
  def asGraphEditorTool[Q >: N](paint: (Colorisation, Expression[Set[Q]]) => Expression[GraphicalContent]) =
    {
      def graphics(viewport: Viewport, hasFocus: Expression[Boolean]) : Expression[GraphicalContent] =
        (paint(ConnectionTool.highlightedColorisation, for (mo <- mouseOver) yield mo match {
          case None => Set.empty[Q]
          case Some(n) => Set(n)
              }) <**>
            connectingLineGraphics(viewport, hasFocus)) (_.compose(_))

     ToolHelper.asGraphEditorTool(Some(mouseListener), None, Some(graphics), Some(hintGraphics), None, GenericConnectionTool.button)
    }
}

object ConnectionTool {
  val highlightedColorisation = new Colorisation {
    override def getColorisation = new Color(99, 130, 191).brighter()
    override def getBackground = null
  }

  def create[N](
    components: Expression[_ <: Iterable[N]],
    touchableProvider: N => Expression[Touchable],
    centerProvider: N => Expression[Point2D.Double],
    connectionController: ConnectionManager[N]) = {
    val connectionHitTester = HitTester.create(components, touchableProvider)
    val genericConnectionTool = new GenericConnectionTool[N](centerProvider, connectionController, connectionHitTester.hitTest(_: Point2D.Double))
    
    new ConnectionTool[N] ( genericConnectionTool.mouseListener, (viewport, focus) => genericConnectionTool.userSpaceContent(viewport, focus),
        (viewport, focus) => genericConnectionTool.screenSpaceContent(viewport, focus), genericConnectionTool.mouseOverNode)
  }
}
