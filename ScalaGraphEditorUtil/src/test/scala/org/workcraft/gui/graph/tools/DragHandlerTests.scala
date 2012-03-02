package org.workcraft.gui.graph.tools

import org.junit.Assert.assertEquals
import java.awt.geom.Point2D
import org.junit.Test
import org.workcraft.gui.graph.tools.selection.MoveDragHandler
import org.workcraft.scala.Expressions._
import pcollections.PCollection
import pcollections.TreePVector
import org.workcraft.dependencymanager.advanced.user.Variable

case class Dummy {
	val coordinate = Variable.create[Point2D.Double](new Point2D.Double(0,0));
}

object DragHandlerTests {
  private def init(selectionV : List[Dummy]) : MoveDragHandler[Dummy] = {
    val selection = constant(selectionV)
    def movableController(node : Dummy) = Some(node.coordinate : ModifiableExpression[Point2D.Double])
    def snap (arg : Point2D.Double) = arg
    new MoveDragHandler[Dummy](selection, movableController(_), snap(_))
  }

  @Test
  def testSimpleSingleNodeMove = {
    val node = new Dummy
    val selection = List(node)
    val dragger = init(selection)
    val drag = dragger.startDrag(node)
    drag.setOffset(new Point2D.Double(1, 1))
    assertEquals(new Point2D.Double(1, 1), node.coordinate.getValue)
  }
  
  @Test
  def testSimpleSingleNodeDoubleMove {
    val node = new Dummy
    val selection = List(node)
    val dragger = init(selection)
    val drag = dragger.startDrag(node)
    drag.setOffset(new Point2D.Double(1, 1))
    drag.setOffset(new Point2D.Double(2, 2))
    assertEquals(new Point2D.Double(2, 2), node.coordinate.getValue)
  }
}
