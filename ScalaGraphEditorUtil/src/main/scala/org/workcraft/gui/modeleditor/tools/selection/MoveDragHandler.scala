package org.workcraft.gui.modeleditor.tools.selection

import java.awt.geom.Point2D
import java.util.HashMap
import org.workcraft.scala.Expressions._
import org.workcraft.scala.effects.IO
import scalaz._
import Scalaz._
import org.workcraft.util.Geometry
import org.workcraft.gui.modeleditor.tools.DragHandler
import org.workcraft.gui.modeleditor.tools.DragHandle

class MoveDragHandler[Node]
   (selection : Expression[Iterable[Node]]
    , movableController : Node => Option[ModifiableExpression[Point2D.Double]] 
    , snap : Point2D.Double => Point2D.Double)
      extends DragHandler[Node] {

  override def dragStarted(pos: Point2D.Double, hitNode : Node) = new DragHandle {
      val originalPosition = getNodePos(hitNode, movableController)
      
      var originalPositions : HashMap[Node, Point2D.Double] = new HashMap[Node, Point2D.Double]
      
      def getOriginalPositionWithDefault(node : Node, pos : Point2D.Double) : Point2D.Double = {
        val res = originalPositions.get(node)
        if(res != null)
          return res
        else {
          originalPositions.put(node, pos)
          return pos
        }
      }
      
      private def offsetSelection (totalOffset : Point2D.Double) = selection.eval >>= (nodes => {
          nodes.toList.traverse_ (node => movableController(node) match {
            case None => IO.Empty
            case Some(pos) => pos.update(posVal => Geometry.add(getOriginalPositionWithDefault(node, posVal), totalOffset))
          })
        }
      )
      
      def snapper(newValue : Point2D.Double) = {
          val newSnapped = snap(newValue)
          val totalOffset = Geometry.subtract(newSnapped, originalPosition)
          offsetSelection(totalOffset)
        }
  
      snapper(originalPosition)
      
      def getNodePos(hitNode : Node, movableController : Node => Option[ModifiableExpression[Point2D.Double]]) : Point2D.Double =
        movableController(hitNode).map{arg => arg.unsafeEval}.getOrElse(new Point2D.Double(0,0))
      
      override def dragged(pos : Point2D.Double) = snapper(pos)
      
      override def commit = IO.Empty
      
      override def cancel = dragged (originalPosition)
  }
}