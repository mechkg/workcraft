package org.workcraft.gui.modeleditor


import org.workcraft.scala.Expressions._
import scalaz._
import Scalaz._

import org.workcraft.graphics.GraphicalContent
import org.workcraft.dependencymanager.advanced.user.Variable
import java.awt.Color
import java.awt.Font
import java.awt.BasicStroke

class Ruler (val grid: Grid, val viewport: Viewport, val dimensions: Expression[(Int, Int, Int, Int)]) {
  def graphicalContent: Expression[GraphicalContent] = for {
    dimensions <- dimensions;
    size <- Ruler.size;
    font <- Ruler.font;
    background <- Ruler.backgroundColor;
    foreground <- Ruler.foregroundColor;
    stroke <- Ruler.tickStroke;
    minorTickSize <- Ruler.minorTickSize;
    majorTickSize <- Ruler.majorTickSize;
    gridLines <- grid.gridLines
    userToScreenX <- viewport.userToScreenX;
    userToScreenY <- viewport.userToScreenY
  } yield GraphicalContent ( g => {
    val (x, y, width, height) = dimensions
    
    val (majorH, majorV, minorH, minorV) = gridLines
    
    g.setBackground(background)
    g.clearRect(x, y, width, size)
    g.clearRect(x, y + size, size, height-size)
    
    g.setColor(foreground)
    g.setStroke(stroke)
    g.drawLine(x, size, width, size)
    g.drawLine(size, y, size, height)
    
    if (minorTickSize > 0) {
      minorV.foreach( tx => g.drawLine (x + userToScreenX(tx), size + y, x + userToScreenX(tx), size + y - minorTickSize))
      minorH.foreach( ty => g.drawLine (x + size, userToScreenY(ty) + y, x + size - minorTickSize, userToScreenY(ty) + y))
    }
    
    g.setFont(font)
    
    if (majorTickSize >0) {
      majorV.foreach( tx => {
        g.drawLine (x + userToScreenX(tx), size + y, x + userToScreenX(tx), size + y - majorTickSize)
        g.drawString(String.format("%.2f", tx.asInstanceOf[java.lang.Object]), x + userToScreenX(tx) + 2, y + size - 5)
      })
      
      majorH.foreach( ty => {
       g.drawLine (x + size, userToScreenY(ty) + y, x + size - majorTickSize, userToScreenY(ty) + y)
       val re = g.getTransform()
       g.translate (x+size-5, y + userToScreenY(ty) - 2)
       g.rotate(-Math.Pi/2)
       g.drawString (String.format("%.2f", ty.asInstanceOf[java.lang.Object]), 0, 0)
       g.setTransform(re)
      })
    }
  })
}

object Ruler {
  val backgroundColor = Variable.create (new Color(225, 231, 242))
  val foregroundColor = Variable.create (new Color(0, 0, 0))
  val size = Variable.create (15)
  val tickStroke = Variable.create(new BasicStroke(1))
  val majorTickSize = Variable.create (10)
  val minorTickSize = Variable.create (3)
  val font = Variable.create (new Font (Font.SANS_SERIF, 0, 9))
}