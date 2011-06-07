package org.workcraft.plugins.cpog.scala.tools
import org.workcraft.gui.graph.tools.GraphEditorMouseListener
import org.workcraft.gui.graph.tools.GraphEditorKeyListener
import org.workcraft.dependencymanager.advanced.core.Expression
import org.workcraft.dom.visual.GraphicalContent
import org.workcraft.gui.graph.tools.GraphEditorTool.Button
import org.workcraft.gui.graph.tools.AbstractTool
import javax.swing.JPanel
import javax.swing.Icon
import org.workcraft.gui.graph.tools.GraphEditorTool
import org.workcraft.gui.graph.tools.DummyKeyListener
import org.workcraft.gui.graph.tools.DummyMouseListener
import org.workcraft.gui.graph.Viewport
import org.workcraft.plugins.cpog.scala.Expressions

object ToolHelper {
  def asGraphEditorTool (
      mouselistener: Option[GraphEditorMouseListener],
      keylistener: Option[GraphEditorKeyListener],
      userSpaceGraphics: Option[(Viewport, Expression[java.lang.Boolean]) => Expression[GraphicalContent]],
      screenSpaceGraphics: Option[(Viewport, Expression[java.lang.Boolean]) => Expression[GraphicalContent]],
      interfacePanel: Option[JPanel],
      button: Button
      ) =
        new GraphEditorTool {
	  		override def getButton = button
	  		override def keyListener = keylistener match {
	  		  case Some(listener) => listener
	  		  case None => DummyKeyListener.INSTANCE
	  		}
	  		override def mouseListener = mouselistener match {
	  		  case Some(listener) => listener
	  		  case None => DummyMouseListener.INSTANCE
	  		} 
	  		override def activated = {}
	  		override def deactivated = {}
	  		override def userSpaceContent (viewport: Viewport, hasFocus : Expression[java.lang.Boolean]) = userSpaceGraphics match {
	  		  case Some(graphics) => graphics(viewport, hasFocus)
	  		  case None => Expressions.constant(GraphicalContent.EMPTY)
	  		}
	  		override def screenSpaceContent (viewport: Viewport, hasFocus : Expression[java.lang.Boolean]) = screenSpaceGraphics match {
	  		  case Some(graphics) => graphics(viewport, hasFocus)
	  		  case None => Expressions.constant(GraphicalContent.EMPTY)
	  		}
	  		override def getInterfacePanel = interfacePanel match {
	  		  case Some(panel) => panel
	  		  case None => null
	  		}
	  		
  }
}