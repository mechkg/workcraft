package org.workcraft.plugins.cpog.scala.tools

import org.workcraft.dom.visual.ColorisableGraphicalContent
import org.workcraft.gui.graph.tools.GraphEditorToolUtil
import org.workcraft.gui.graph.tools.Colorisation
import org.workcraft.gui.graph.tools.AbstractTool
import org.workcraft.gui.graph.tools.GraphEditorTool.Button
import org.workcraft.dom.visual.GraphicalContent
import org.workcraft.gui.graph.Viewport
import org.workcraft.plugins.cpog.CustomToolsProvider
import org.workcraft.gui.graph.tools.selection.GenericSelectionTool
import java.awt.Color
import java.awt.BasicStroke
import org.workcraft.dom.visual.BoundedColorisableGraphicalContent
import java.awt.geom.Point2D
import org.workcraft.gui.graph.tools.selection.MoveDragHandler
import org.workcraft.plugins.cpog.scala.nodes._
import org.workcraft.dependencymanager.advanced.core.Expression
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression
import pcollections.HashTreePSet
import pcollections.PSet
import org.workcraft.plugins.cpog.scala.Util._
import org.workcraft.plugins.cpog.scala.Expressions._
import org.workcraft.plugins.cpog.scala.VisualArc._
import scala.collection.JavaConversions.{ collectionAsScalaIterable, asJavaCollection }
import org.workcraft.util.Maybe.Util.just
import org.workcraft.plugins.cpog.gui.TouchableProvider.bbToTouchable
import org.workcraft.plugins.cpog.scala.Graphics._
import java.awt.geom.Path2D
import java.awt.geom.Ellipse2D
import org.workcraft.dom.visual.Touchable
import org.workcraft.plugins.cpog.scala.ControlPoint
import org.workcraft.plugins.cpog.scala.VisualArc


object ControlPointsTool {
  import org.workcraft.plugins.cpog.scala.Scalaz._
  
  val controlPointSize = 0.15
  
  def controlPointGraphics (position : Point2D) =
    circle(controlPointSize, None, Some(Color.BLUE)) translate position
  
  def bezierControlPointGraphics(position : Expression[Point2D], vertexPosition : Expression[Point2D]) = 
    for (
        position <- position;
        vertexPosition <- vertexPosition
    ) yield {
      
     val p = new Path2D.Double()
     p.moveTo(vertexPosition.getX, vertexPosition.getY)
     p.lineTo(position.getX, position.getY)
     
     val cpg = controlPointGraphics(position)
     
     cpg over (path (p, new BasicStroke(0.02f), Color.GRAY.brighter, 0), cpg.touchable) 
  }
  
  def polylineControlPointGraphics (position : Expression[Point2D]) =
  for (
        position <- position
    ) yield 
    controlPointGraphics(position)
  
  def create (selection: Expression[Set[Node]], painter : Expression[GraphicalContent]) = {
    val snap: Point2D => Point2D = x => x
	val highlightedColorisation = new Colorisation {
			override def getColorisation = new Color(99, 130, 191).brighter();
			override def getBackground = null
		}

    val selectedControlPoints = org.workcraft.dependencymanager.advanced.user.Variable.create[PSet[ControlPoint]](HashTreePSet.empty());

    def getNodeControlPoints(node: Node): Expression[List[ControlPoint]] = node match {
      case arc@Arc(_, _, _, visual) => for (visual <- visual : Expression[VisualArc]) yield visual match {
        case Polyline(cps) => cps.map(x => new ControlPoint(x, polylineControlPointGraphics(x)))
        case Bezier(cp1, cp2) => new ControlPoint(cp1, bezierControlPointGraphics(cp1, arc.first.visualProperties.position)) ::
                                 new ControlPoint(cp2, bezierControlPointGraphics(cp2, arc.second.visualProperties.position)) :: Nil
      }
      case Component(_) => constant(Nil)
    }
    
    val visibleControlPoints : Expression[java.util.Collection[_ <: ControlPoint]] = 
      for ((selectedNodes : Set[Node]) <- selection;
    	nodeLists : List[List[ControlPoint]] <- joinCollection(selectedNodes.map(getNodeControlPoints)))
    	yield asJavaCollection[ControlPoint](nodeLists.flatten : List[ControlPoint]): java.util.Collection[ControlPoint]
    
    def controlPointMovableController(cp: ControlPoint) = just(cp.position)
    val cpDragHandler = new MoveDragHandler[ControlPoint](selectedControlPoints, asFunctionObject(controlPointMovableController), asFunctionObject(snap))
        
    val cpHitTester = CustomToolsProvider.createHitTester[ControlPoint](visibleControlPoints, asFunctionObject((cp : ControlPoint) => for (g <- cp.graphics) yield g.touchable))
    def cpPainter(cp: ControlPoint) = for (g <- cp.graphics) yield g.graphics
    
    val gcpet = new GenericSelectionTool[ControlPoint](selectedControlPoints, cpHitTester, cpDragHandler)
    val controlPointGC = CustomToolsProvider.drawWithHighlight[ControlPoint](highlightedColorisation, selectedControlPoints, asFunctionObject(cpPainter), visibleControlPoints);
    val controlPointEditorTool = new AbstractTool {
      override def mouseListener = gcpet.getMouseListener
      override def userSpaceContent (viewport : Viewport, hasFocus : Expression[java.lang.Boolean]) = gcpet.userSpaceContent(viewport)
      override def screenSpaceContent (viewport : Viewport, hasFocus : Expression[java.lang.Boolean]) = constant(GraphicalContent.EMPTY)
      override def getButton = new Button {
        override def getLabel = "Control point editor"
        override def getIcon = null
        override def getHotKeyCode = java.awt.event.KeyEvent.VK_Q
      }
    }
    GraphEditorToolUtil.attachPainter(controlPointEditorTool,  for((p : GraphicalContent) <- painter; cpgc <- controlPointGC) yield org.workcraft.util.Graphics.compose(p, cpgc))
  }
}
