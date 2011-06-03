package org.workcraft.plugins.cpog.scala.tools
import org.workcraft.gui.graph.tools.GenericConnectionTool
import org.workcraft.plugins.cpog.scala.nodes._
import org.workcraft.plugins.cpog.scala.Util._
import org.workcraft.plugins.cpog.scala.Graphics._
import org.workcraft.plugins.cpog.scala.Scalaz._
import org.workcraft.plugins.cpog.scala.Expressions._
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

object ConnectionTool {
  val highlightedColorisation = new Colorisation {
    override def getColorisation = new Color(99, 130, 191).brighter()
    override def getBackground = null
  }  
  
  def create (
        components : Expression[_ <: Iterable[Component]], 
        touchable: Component => Expression[Touchable],
        painter: (Component => Expression[Colorisation]) => Expression[GraphicalContent],
        controller: ConnectionController[Component]
        ) = {
    val connectionHitTester = HitTester.create (components, touchable) 
    val genericConnectionTool = new GenericConnectionTool[Component](MovableController.position(_:Component), controller, connectionHitTester.hitTest(_:Point2D))
    
    new AbstractTool {
      override def mouseListener = genericConnectionTool.mouseListener
      override def userSpaceContent(viewport: Viewport, hasFocus: Expression[java.lang.Boolean]) = 
          compose (
                    painter ( c => for (over <- genericConnectionTool.mouseOverNode) yield if (over == c) highlightedColorisation else Colorisation.EMPTY ),
                    genericConnectionTool.userSpaceContent(viewport, hasFocus)
                  )
      override def screenSpaceContent(viewport: Viewport, hasFocus: Expression[java.lang.Boolean]) = genericConnectionTool.screenSpaceContent(viewport, hasFocus)
      override def getButton = genericConnectionTool.button
    }
  }
}