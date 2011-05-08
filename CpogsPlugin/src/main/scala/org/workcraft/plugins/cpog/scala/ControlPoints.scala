package org.workcraft.plugins.cpog.scala

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
import org.workcraft.plugins.cpog.ControlPoint
import nodes._
import org.workcraft.dependencymanager.advanced.core.Expression
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression
import pcollections.HashTreePSet
import pcollections.PSet
import Util._
import org.workcraft.dependencymanager.advanced.core.Expressions._
import VisualArc._
import scala.collection.JavaConversions.{ collectionAsScalaIterable, asJavaCollection }
import org.workcraft.util.Maybe.Util.just
import org.workcraft.plugins.cpog.gui.TouchableProvider.bbToTouchable;

object ControlPoints {
  def gogo (selection: Expression[_ <: Collection[_ <: Node]], painter : Expression[GraphicalContent]) = {
    val snap: Point2D => Point2D = x => x
	val highlightedColorisation = new Colorisation {
			override def getColorisation = new Color(99, 130, 191).brighter();
			override def getBackground = null
		}

    val selectedControlPoints = org.workcraft.dependencymanager.advanced.user.Variable.create[PSet[ControlPoint]](HashTreePSet.empty());

    def getNodeControlPoints(node: Node): Expression[List[ControlPoint]] = node match {
      case Arc(_, _, _, visual) => for (visual <- visual) yield visual match {
        case Polyline(cps) => cps.map(x => new ControlPoint(x))
        case Bezier(cp1, cp2) => new ControlPoint(cp1) :: new ControlPoint(cp2) :: Nil
      }
      case Component(_) => constant(Nil)
    }
    
    val visibleControlPoints = for (selectedNodes <- selection;
    	nodeLists <- joinCollection(asJavaCollection(collectionAsScalaIterable[Node](selectedNodes).map(getNodeControlPoints)))
      ) yield asJavaCollection(collectionAsScalaIterable(nodeLists).flatten)
    
    def controlPointMovableController(cp: ControlPoint) = just(cp.position)
    val cpDragHandler = new MoveDragHandler[ControlPoint](selectedControlPoints, asFunctionObject(controlPointMovableController), asFunctionObject(snap))
    def cpGc(cp: ControlPoint) = for (position <- cp.position)
      yield BoundedColorisableGraphicalContent.translate(org.workcraft.plugins.cpog.scala.Graphics.boundedCircle(1, new BasicStroke(0), Color.BLUE, Color.RED), position);
    val cpHitTester = CustomToolsProvider.createHitTester[ControlPoint](visibleControlPoints, asFunctionObject((cp : ControlPoint) => for (gc <- cpGc(cp)) yield bbToTouchable(gc.boundingBox)))
    def cpPainter(cp: ControlPoint) = for (gc <- cpGc(cp)) yield gc.graphics
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
    GraphEditorToolUtil.attachPainter(controlPointEditorTool,  for(p <- painter; cpgc <- controlPointGC) yield org.workcraft.util.Graphics.compose(p, cpgc))
  }
}
