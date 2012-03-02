package org.workcraft.gui.graph.tools.selection

import java.awt.geom.Point2D
import java.util.HashMap

import org.workcraft.scala.Expressions._
import org.workcraft.gui.graph.tools.DragHandle
import org.workcraft.gui.graph.tools.DragHandler
import org.workcraft.util.Geometry

import pcollections.PCollection

class MoveDragHandler[Node]
   ( selection : Expression[Iterable[Node]]
     , movableController : Node => Option[ModifiableExpression[Point2D.Double]] 
     , snap : Point2D.Double => Point2D.Double)
      extends DragHandler[Node] {
  
  override def startDrag(hitNode : Node) : DragHandle = new DragHandle {

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
      
      private def offsetSelection (totalOffset : Point2D.Double) ={
        for(node <- unsafeEval(selection)) {
          movableController.apply(node) match{
            case None => {}
            case Some(pos) => {
              val posVal = unsafeEval(pos)
              val origPosVal = getOriginalPositionWithDefault(node, posVal)
              pos.setValue(Geometry.add(origPosVal, totalOffset))
            }
          }
        }
      }
      
      def snapper(newValue : Point2D.Double) = {
          val newSnapped = snap.apply(newValue)
          val totalOffset = Geometry.subtract(newSnapped, originalPosition)
          offsetSelection(totalOffset)
        }
  
      snapper(originalPosition)
      
      def getNodePos(hitNode : Node, movableController : Node => Option[ModifiableExpression[Point2D.Double]]) : Point2D.Double =
        movableController(hitNode).map{arg => unsafeEval(arg)}.getOrElse(new Point2D.Double(0,0))
      
      override def setOffset(offset : Point2D.Double) =
        snapper(Geometry.add(originalPosition, offset))
      
      override def commit = { }
      
      override def cancel =
        setOffset(new Point2D.Double(0,0))
  }
}
