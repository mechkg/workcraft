package org.workcraft.scala.grapheditor.tools
import org.workcraft.dependencymanager.advanced.core.{Expression => JExpression}
import org.workcraft.gui.graph.tools.DummyKeyListener
import org.workcraft.gui.graph.tools.DummyMouseListener
import org.workcraft.gui.graph.tools.GraphEditorKeyListener
import org.workcraft.gui.graph.tools.GraphEditorMouseListener
import org.workcraft.gui.graph.tools.GraphEditorTool
import org.workcraft.gui.modeleditor.ToolMouseListener
import org.workcraft.gui.graph.Viewport
import org.workcraft.scala.Expressions._
import javax.swing.JPanel
import org.workcraft.graphics.GraphicalContent
import org.workcraft.scala.Scalaz._
import org.workcraft.dom.visual.{GraphicalContent => JGraphicalContent}
import java.awt.Graphics2D
import org.workcraft.gui.modeleditor.KeyBinding
import org.workcraft.gui.modeleditor.tools.ModelEditorTool
import org.workcraft.gui.modeleditor.tools.Button

object ToolHelper {
  private def toJava(gc : GraphicalContent) : JGraphicalContent = new JGraphicalContent{
    override def draw(g : Graphics2D) = gc.draw(g)
  }
  def asModelEditorTool (
      mouselistener: Option[ToolMouseListener],
      keylistener: List[KeyBinding],
      userSpaceGraphics: Expression[GraphicalContent],
      screenSpaceGraphics: Expression[GraphicalContent],
      interfacePanel: Option[JPanel],
      buttoon: Button
      ) =
        new ModelEditorTool {
	  		override def button = buttoon
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
	  		override def userSpaceContent (viewport: Viewport, hasFocus : JExpression[java.lang.Boolean]) : JExpression[_ <: JGraphicalContent] = (userSpaceGraphics match {
	  		  case Some(graphics) => graphics(viewport, hasFocus.map(f => f : Boolean))
	  		  case None => constant(GraphicalContent.Empty)
	  		}).map(toJava(_)).jexpr
	  		override def screenSpaceContent (viewport: Viewport, hasFocus : JExpression[java.lang.Boolean]) : JExpression[_ <: JGraphicalContent] = (screenSpaceGraphics match {
	  		  case Some(graphics) => graphics(viewport, hasFocus.map(f => f : Boolean))
	  		  case None => constant(GraphicalContent.Empty)
	  		}).map(toJava(_)).jexpr
	  		override def getInterfacePanel = interfacePanel match {
	  		  case Some(panel) => panel
	  		  case None => null
	  		}
  }
}
