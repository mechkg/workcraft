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
import pcollections.HashTreePSet
import pcollections.PSet
import org.workcraft.scala.Util._
import org.workcraft.scala.Scalaz._
import org.workcraft.scala.Expressions._
import org.workcraft.plugins.cpog.scala.VisualArc._
import scala.collection.JavaConversions.{ collectionAsScalaIterable, asJavaCollection }
import org.workcraft.util.Maybe.Util.just
import org.workcraft.plugins.cpog.gui.TouchableProvider.bbToTouchable
import org.workcraft.graphics.Graphics._
import java.awt.geom.Path2D
import java.awt.geom.Ellipse2D
import org.workcraft.dom.visual.Touchable
import org.workcraft.plugins.cpog.scala.ControlPoint
import org.workcraft.plugins.cpog.scala.VisualArc
import org.workcraft.dom.visual.connections.RelativePoint
import org.workcraft.dependencymanager.advanced.core.{ Expressions => JExpressions }
import org.workcraft.dependencymanager.advanced.user.Setter
import org.workcraft.util.Maybe
import org.workcraft.gui.graph.tools.GraphEditorMouseListener
import org.workcraft.graphics.GraphicsHelper
import org.workcraft.scala.grapheditor.tools._

class ControlPointsTool (val mouseListener: GraphEditorMouseListener,
    userSpaceGraphics: (Viewport, Expression[java.lang.Boolean]) => Expression[GraphicalContent]) {
  def asGraphEditorTool (modelGraphics : Expression[GraphicalContent]) = {
    def graphics (viewport:Viewport, hasFocus: Expression[java.lang.Boolean]) =
      (modelGraphics <**> userSpaceGraphics (viewport, hasFocus)) (compose(_, _))
      
    ToolHelper.asGraphEditorTool(Some(mouseListener), None, Some(graphics), None, None, ControlPointsTool.button)
  }
}

object ControlPointsTool {
  import org.workcraft.scala.Scalaz._

  val controlPointSize = 0.15

  val highlightedColorisation = new Colorisation {
    override def getColorisation = new Color(99, 130, 191).brighter();
    override def getBackground = null
  }
  
  val button = new Button {
    override def getLabel = "Control point editor"
    override def getIcon = null
    override def getHotKeyCode = java.awt.event.KeyEvent.VK_Q
  }  

  def controlPointGraphics(position: Point2D) =
    circle(controlPointSize, None, Some(Color.BLUE)) translate position

  def bezierControlPointGraphics(position: Point2D, vertexPosition: Point2D) =
    {
      val p = new Path2D.Double()
      p.moveTo(vertexPosition.getX, vertexPosition.getY)
      p.lineTo(position.getX, position.getY)

      val cpg = controlPointGraphics(position)

      cpg over (path(p, new BasicStroke(0.02f), Color.GRAY.brighter, 0), cpg.touchable)
    }

  def create(selectedArcs: Expression[List[(Point2D, Point2D, VisualArc)]], snap: Point2D => Point2D) = {

    val selectedControlPoints = org.workcraft.dependencymanager.advanced.user.Variable.create[PSet[ControlPoint]](HashTreePSet.empty())

    def getControlPoints(arc: (Point2D, Point2D, VisualArc)): List[ControlPoint] =
      {
        val (firstPos, secondPos, visual) = arc
        visual match {
          case Polyline(cps) => cps.map(x => new ControlPoint(x, for (x <- x) yield controlPointGraphics(x)))
          case Bezier(cp1, cp2) => {

            def convertCp(cp: ModifiableExpression[RelativePoint]) = ModifiableExpression(
              for (cp <- cp) yield cp.toSpace(firstPos, secondPos),
              (v: Point2D) => Maybe.Util.doIfJust(RelativePoint.fromSpace(firstPos, secondPos, v), cp.setValue)
              )
            val cp1_ = convertCp(cp1)
            val cp2_ = convertCp(cp2)

            new ControlPoint(cp1_, for (cp1 <- cp1_) yield bezierControlPointGraphics(cp1, firstPos)) ::
            new ControlPoint(cp2_, for (cp2 <- cp2_) yield bezierControlPointGraphics(cp2, secondPos)) :: Nil
          }
        }
      }
    
    // FIXME: eliminate the need for this
    val visibleControlPoints: Expression[List[ControlPoint]] =
      for (
        (selectedArcs) <- selectedArcs;
        val nodeLists = selectedArcs.map(getControlPoints).toList
      ) yield nodeLists.flatten: List[ControlPoint]
    

    val visibleControlPointsJ: Expression[java.util.Collection[_ <: ControlPoint]] =
      for (vcp <- visibleControlPoints) yield asJavaCollection[ControlPoint](vcp)
    

    def controlPointMovableController(cp: ControlPoint) = just(cp.position)
    val cpDragHandler = new MoveDragHandler[ControlPoint](selectedControlPoints, asFunctionObject(((x : Maybe[ModifiableExpression[Point2D]]) => asMaybe(for(x <- x) yield x.jexpr)) compose controlPointMovableController), asFunctionObject(snap))

    val cpHitTester = CustomToolsProvider.createHitTester[ControlPoint](visibleControlPointsJ.jexpr, asFunctionObject((cp: ControlPoint) => for (g <- cp.graphics) yield g.touchable))
    def cpPainter(cp: ControlPoint) = for (g <- cp.graphics) yield g.graphics

    val gcpet = new GenericSelectionTool[ControlPoint](selectedControlPoints, cpHitTester, cpDragHandler)
    val controlPointGC =
      GraphicsHelper.paintWithHighlights[ControlPoint](cpPainter, visibleControlPoints) (highlightedColorisation, selectedControlPoints)
    
    new ControlPointsTool (gcpet.getMouseListener, (viewport, _) => (controlPointGC <**> gcpet.userSpaceContent(viewport)) (compose(_,_)))
  }
}
