package org.workcraft.plugins.cpog.scala.tools
import org.workcraft.gui.graph.tools.GenericConnectionTool
import org.workcraft.plugins.cpog.scala.nodes._
import org.workcraft.scala.Util._
import org.workcraft.graphics.Graphics._
import org.workcraft.scala.Scalaz._
import org.workcraft.scala.Expressions._
import org.workcraft.dependencymanager.advanced.core.Expression
import org.workcraft.dom.visual.Touchable
import org.workcraft.dom.visual.ColorisableGraphicalContent
import org.workcraft.gui.graph.tools.ConnectionController
import java.awt.geom.Point2D
import org.workcraft.gui.graph.tools.AbstractTool
import org.workcraft.gui.graph.Viewport
import org.workcraft.gui.graph.tools.Colorisation
import org.workcraft.dom.visual.GraphicalContent
import java.awt.Color
import org.workcraft.gui.graph.tools.GraphEditorMouseListener
import org.workcraft.gui.graph.tools.GraphEditorKeyListener
import org.workcraft.util.Maybe
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression
import org.workcraft.tools._

class ConnectionTool[N](val mouseListener: GraphEditorMouseListener,
  val connectingLineGraphics: (Viewport, Expression[java.lang.Boolean]) => Expression[GraphicalContent],
  val hintGraphics: (Viewport, Expression[java.lang.Boolean]) => Expression[GraphicalContent],
  val mouseOver: Expression[N]) {
  
  def asGraphEditorTool[Q >: N](paint: (Colorisation, Expression[_ <: java.util.Set[Q]]) => Expression[GraphicalContent]) =
    {
      def graphics(viewport: Viewport, hasFocus: Expression[java.lang.Boolean]) =
        compose(paint(ConnectionTool.highlightedColorisation, for (mo <- mouseOver) yield java.util.Collections.singleton(mo)),
            connectingLineGraphics(viewport, hasFocus))

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
    centerProvider: N => Expression[Point2D],
    connectionController: ConnectionController[N]) = {
    val connectionHitTester = HitTester.create(components, touchableProvider)
    val genericConnectionTool = new GenericConnectionTool[N](centerProvider, connectionController, connectionHitTester.hitTest(_: Point2D))
    
    new ConnectionTool[N] ( genericConnectionTool.mouseListener, (viewport, focus) => genericConnectionTool.userSpaceContent(viewport, focus),
        (viewport, focus) => genericConnectionTool.screenSpaceContent(viewport, focus), genericConnectionTool.mouseOverNode)
  }
}