package org.workcraft.scala.grapheditor.tools

import org.workcraft.gui.graph.tools.GenericConnectionTool
import org.workcraft.scala.Util._
import org.workcraft.scala.Scalaz._
import org.workcraft.scala.Expressions._
import org.workcraft.dom.visual.Touchable
import org.workcraft.dom.visual.ColorisableGraphicalContent
import org.workcraft.gui.graph.tools.ConnectionController
import java.awt.geom.Point2D
import org.workcraft.gui.graph.tools.AbstractTool
import org.workcraft.gui.graph.Viewport
import org.workcraft.gui.graph.tools.Colorisation
import org.workcraft.dom.visual.GraphicalContent
import org.workcraft.dom.visual.GraphicalContent.Util.compose
import java.awt.Color
import org.workcraft.gui.graph.tools.GraphEditorMouseListener
import org.workcraft.gui.graph.tools.GraphEditorKeyListener
import org.workcraft.util.Maybe
import org.workcraft.util.MaybeVisitor

class ConnectionTool[N](val mouseListener: GraphEditorMouseListener,
  val connectingLineGraphics: (Viewport, Expression[java.lang.Boolean]) => Expression[GraphicalContent],
  val hintGraphics: (Viewport, Expression[java.lang.Boolean]) => Expression[GraphicalContent],
  val mouseOver: Expression[Maybe[_ <: N]]) {
  
  def asGraphEditorTool[Q >: N](paint: (Colorisation, Expression[java.util.Set[Q]]) => Expression[GraphicalContent]) =
    {
      def graphics(viewport: Viewport, hasFocus: Expression[java.lang.Boolean]) : Expression[GraphicalContent] =
        (paint(ConnectionTool.highlightedColorisation, for (mo <- mouseOver) yield mo.accept(new MaybeVisitor[N, java.util.Set[Q]] {
                override def visitNothing = java.util.Collections.emptySet[Q]
                override def visitJust(n : N) =  java.util.Collections.singleton(n)
              })) <**>
            connectingLineGraphics(viewport, hasFocus)) (compose(_,_))

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
    connectionController: ConnectionController[N]) = {
    val connectionHitTester = HitTester.create(components, touchableProvider)
    val genericConnectionTool = new GenericConnectionTool[N](((_:Expression[Point2D.Double]).jexpr) compose centerProvider, connectionController, connectionHitTester.hitTest(_: Point2D.Double))
    
    new ConnectionTool[N] ( genericConnectionTool.mouseListener, (viewport, focus) => genericConnectionTool.userSpaceContent(viewport, focus.jexpr),
        (viewport, focus) => genericConnectionTool.screenSpaceContent(viewport, focus.jexpr), genericConnectionTool.mouseOverNode)
  }
}
