package org.workcraft.plugins.cpog.scala

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
import nodes._
import org.workcraft.dependencymanager.advanced.core.{Expressions => JExpressions}
import org.workcraft.dependencymanager.advanced.core.Expression
import org.workcraft.dependencymanager.advanced.core.GlobalCache
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression
import pcollections.HashTreePSet
import pcollections.PSet
import org.workcraft.plugins.cpog.scala.Util._
import org.workcraft.plugins.cpog.scala.Expressions._
import VisualArc._
import scala.collection.JavaConversions.{ collectionAsScalaIterable, asJavaCollection }
import org.workcraft.util.Maybe.Util.just
import org.workcraft.plugins.cpog.gui.TouchableProvider.bbToTouchable
import org.workcraft.plugins.cpog.scala.Graphics._
import java.awt.geom.Path2D
import java.awt.geom.Ellipse2D
import org.workcraft.dom.visual.Touchable
import org.workcraft.dom.visual.connections.RelativePoint
import org.workcraft.dependencymanager.advanced.user.Setter
import org.workcraft.util.Maybe

object ControlPoints {
  import Scalaz._
  
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
  
  def gogo (selection: Expression[Set[Node]], painter : Expression[GraphicalContent]) = {
    val snap: Point2D => Point2D = x => x
	val highlightedColorisation = new Colorisation {
			override def getColorisation = new Color(99, 130, 191).brighter();
			override def getBackground = null
		}

    val selectedControlPoints = org.workcraft.dependencymanager.advanced.user.Variable.create[PSet[ControlPoint]](HashTreePSet.empty());

    def getNodeControlPoints(node: Node): Expression[List[ControlPoint]] = node match {
      case arc@Arc(_, _, _, visual) => for (visual <- visual : Expression[VisualArc]) yield visual match {
        case Polyline(cps) => cps.map(x => new ControlPoint(x, polylineControlPointGraphics(x)))
        case Bezier(cp1, cp2) => {
          val p1 = arc.first.visualProperties.position
          val p2 = arc.second.visualProperties.position
          def convertCp(cp : ModifiableExpression[RelativePoint]) = JExpressions.modifiableExpression(
              for(cp <- cp; p1 <- p1; p2 <- p2) yield cp.toSpace(p1, p2),
              new Setter[Point2D] {
            	  override def setValue(v : Point2D) = {
            	    val p1_ = GlobalCache.eval(p1)
            	    val p2_ = GlobalCache.eval(p2)
            	    Maybe.Util.doIfJust(RelativePoint.fromSpace(p1_, p2_, v), cp) 
            	  }
              }
              )
          val cp1_ = convertCp(cp1)
          val cp2_ = convertCp(cp2)
          
          new ControlPoint(cp1_, bezierControlPointGraphics(cp1_, p1)) :: 
          new ControlPoint(cp2_, bezierControlPointGraphics(cp2_, p2)) :: Nil
        }
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
