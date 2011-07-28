package org.workcraft.graphics.touchable
import org.workcraft.graphics.Path


import org.junit.Assert._
import java.awt.geom.Point2D
import java.awt.geom.Path2D
class TouchableUtilTests {

  @org.junit.Test
  def testIgnore = {
    val path1 = new Path2D.Double()
    
    path1.lineTo(0,1)
    
    val t = Path.touchable(path1, 0)
    val p1 = new Point2D.Double(0.5, 0)
    val p2 = new Point2D.Double(0, 0)
   
    assertEquals (t.hitTest(p1), false)
    assertEquals (t.hitTest(p2), false)
  }
  
  def simpleHitTest {
    val path1 = new Path2D.Double()
    
    path1.lineTo(0,1)
    
    val t = Path.touchable(path1, 0.01)
    val p1 = new Point2D.Double(0.5, 0)
    val p2 = new Point2D.Double(0, 0)
    val p3 = new Point2D.Double(0.5, 0.005)
    val p4 = new Point2D.Double(0.5, 0.015)
   
    assertEquals (t.hitTest(p1), true)
    assertEquals (t.hitTest(p2), true)
    
    assertEquals (t.hitTest(p3), true)
    assertEquals (t.hitTest(p4), false)
  }
}