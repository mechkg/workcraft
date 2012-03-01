package org.workcraft.scala.grapheditor.tools

import java.awt.geom.Point2D
import org.junit.Test
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression
import org.workcraft.dependencymanager.advanced.user.Variable
import org.workcraft.gui.graph.tools.selection.GenericSelectionTool
import org.workcraft.gui.graph.tools.DragHandler
import org.workcraft.gui.graph.tools.DragHandle

case class Dummy {
	val coordinate = Variable.create[Point2D.Double](new Point2D.Double(0,0));
}

object GenericSelectionToolTests {
  
  @Test
  def test1 = {
    def snap(arg : Point2D.Double) = arg
    val dragHandler = new DragHandler[Dummy] {
      override def startDrag(hitNode : Dummy) = new DragHandle {
          override def setOffset(offset : Point2D.Double) = {}
          override def commit = {}
          override def cancel = {}
        }
    }
    
    val obj = new Dummy
    val selection = Variable.create[Set[Dummy]](Set.empty)
    val hitTester = new HitTester[Dummy] {

      override def hitTest(point : Point2D.Double) = Some(obj)
      override def boxHitTest(start : Point2D.Double, end : Point2D.Double) = null
    }
    new GenericSelectionTool[Dummy](selection, hitTester, dragHandler)
  }
}
