package org.workcraft.gui.modeleditor.tools.selection

import java.awt.BasicStroke
import java.awt.Color
import java.awt.Graphics2D
import java.awt.geom.Point2D
import java.awt.geom.Rectangle2D
import org.workcraft.scala.Expressions._
import org.workcraft.exceptions.NotSupportedException
import org.workcraft.gui.modeleditor.tools.selection.GenericSelectionToolMouseListener.SelectionMode
import org.workcraft.scala.grapheditor.tools.HitTester
import org.workcraft.dependencymanager.advanced.user.Variable
import scalaz._
import Scalaz._
import org.workcraft.graphics.GraphicalContent
import org.workcraft.gui.modeleditor.tools.DragHandle
import org.workcraft.gui.modeleditor.Viewport

object SelectionDragHandler {
  protected val selectionBorderColor = new Color(200, 200, 200)
  protected val selectionFillColor = new Color(99, 130, 191, 32)
}

class SelectionDragHandler[Node](selection : ModifiableExpression[Set[Node]], hitTester : HitTester[Node]) {

  import SelectionDragHandler._
  
  /**
   * Differs from a simple rectangle in that the order of corners does matter.
   */
  case class SelectionRectangle(p1 : Point2D.Double, p2 : Point2D.Double) {
    def asRectangle = new Rectangle2D.Double(
          Math.min(p1.getX, p2.getX),
          Math.min(p1.getY, p2.getY),
          Math.abs(p1.getX-p2.getX),
          Math.abs(p1.getY-p2.getY)
      )
  }
  
  private val selectionBox : Variable[Option[SelectionRectangle]] = Variable.create(None)
  val effectiveSelection :  Expression[Set[Node]] = 
    for(
        sel <- selection
        ; delta <- selectionBoxContents
        ; mode <- selectionMode)
      yield (
            mode match {
              case SelectionMode.Add => sel ++ delta
              case SelectionMode.Remove => sel -- delta
              case SelectionMode.None => sel
              case SelectionMode.Replace => delta
      })
   
  val selectionMode : Variable[SelectionMode] = Variable.create(SelectionMode.None)

  val selectionBoxContents : Expression[Set[Node]] = 
    for(selBox <- selectionBox) yield
      (selBox match {
        case None => Set.empty
        case Some(selBox) => hitTester.boxHitTest(selBox.p1, selBox.p2).toSet
      })
  
  def startDrag(dragStart: Point2D.Double, mode : SelectionMode): DragHandle = {
    selectionMode.setValue(mode)
    new DragHandle {
      override def dragged(pos : Point2D.Double) = selectionBox.set (Some(new SelectionRectangle(dragStart, pos)))
      override def commit = (selection := effectiveSelection) >>=| selectionBox.set(None) >>=| selectionMode.set(SelectionMode.None)
      override def cancel = selectionBox.set(None)
    }
  }
  
  def graphicalContent(viewPort : Viewport) : Expression[GraphicalContent] = for {
    selectionBox <- selectionBox;
    pixelSizeInUserSpace <- viewPort.pixelSizeInUserSpace
  } yield {
    selectionBox match {
      case None => { GraphicalContent.Empty }
      case Some(selBox) =>
        new GraphicalContent {
          override def draw(g : Graphics2D) = {
            g.setStroke(new BasicStroke(pixelSizeInUserSpace.getX.toFloat))
            g.setColor(selectionFillColor)
            g.fill(selBox.asRectangle)
            g.setColor(selectionBorderColor)
            g.draw(selBox.asRectangle)
          }
        }
    }
  }
}
