package org.workcraft.gui.modeleditor.tools.selection

import java.awt.geom.Point2D
import java.util.HashMap
import org.workcraft.scala.Expressions._
import org.workcraft.scala.effects.IO
import scalaz._
import Scalaz._

import org.workcraft.gui.modeleditor.tools.DragHandler
import org.workcraft.gui.modeleditor.tools.DragHandle

class MoveDragHandler[Node]
   ( offset: ModifiableExpression[Point2D.Double]
   , snapOffset: (Node, Point2D.Double) => Point2D.Double
   , commitOperation: IO[Unit] 
   ) extends DragHandler[Node] {

  override def dragStarted(where: Point2D.Double, hitNode: Node) = new DragHandle {
    override def dragged(pos: Point2D.Double) = offset.set(snapOffset(hitNode, new Point2D.Double(pos.getX - where.getX, pos.getY - where.getY)))
    override def commit = commitOperation >>=| offset.set(new Point2D.Double(0,0))
    override def cancel = offset.set(new Point2D.Double(0,0))
  }
}