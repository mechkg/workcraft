package org.workcraft.gui.modeleditor
import org.workcraft.scala.Expressions._
import scalaz._
import Scalaz._
import org.workcraft.graphics.GraphicalContent
import org.workcraft.dependencymanager.advanced.user.Variable
import java.awt.geom.Path2D
import java.awt.Color
import java.awt.geom.Point2D
import org.workcraft.dependencymanager.advanced.core.GlobalCache
import java.awt.geom.Rectangle2D
import java.awt.BasicStroke

class Grid(val viewport: Viewport) {
  def autoInterval: Expression[Double] = for {
    visibleArea <- viewport.visibleArea
    magThreshold <- Grid.magThreshold
    minThreshold <- Grid.minThreshold
    intervalScaleFactor <- Grid.intervalScaleFactor
  } yield {

    val visibleHeight = visibleArea.getHeight

    var majorInterval = 10.0

    while (visibleHeight / majorInterval > magThreshold)
      majorInterval *= intervalScaleFactor
    while (visibleHeight / majorInterval < minThreshold)
      majorInterval /= intervalScaleFactor

    majorInterval
  }

  def gridLines: Expression[(List[Double], List[Double], List[Double], List[Double])] = for {
    visibleArea <- viewport.visibleArea;
    majorInterval <- autoInterval;
    minorIntervalFactor <- Grid.minorIntervalFactor
  } yield {
    def visibleLines(min: Double, max: Double, interval: Double) = Range(Math.ceil(min / interval).toInt, Math.floor(max / interval).toInt+1).map(_*interval).toList
     
    val minorInterval = majorInterval * minorIntervalFactor
    
    (visibleLines(visibleArea.getY, visibleArea.getY + visibleArea.getHeight, majorInterval),
      visibleLines(visibleArea.getX, visibleArea.getX + visibleArea.getWidth, majorInterval),
      visibleLines(visibleArea.getY, visibleArea.getY + visibleArea.getHeight, minorInterval),
      visibleLines(visibleArea.getX, visibleArea.getX + visibleArea.getWidth, minorInterval))
  }

  def gridLinesInScreenSpace: Expression[(List[Int], List[Int], List[Int], List[Int])] = for {
    gridLines <- gridLines;
    userToScreenX <- viewport.userToScreenX;
    userToScreenY <- viewport.userToScreenY
  } yield {
    (gridLines._1.map(userToScreenY(_)),
     gridLines._2.map(userToScreenX(_)),
     gridLines._3.map(userToScreenY(_)),
     gridLines._4.map(userToScreenX(_)))
  }
  
  def graphicalContent: Expression[GraphicalContent] = for {
    lines <- gridLinesInScreenSpace;
    dimensions <- viewport.dimensions;
    majorLinesStroke <- Grid.majorLinesStroke;
    minorLinesStroke <- Grid.minorLinesStroke;
    majorLinesColor <- Grid.majorLinesColor;
    minorLinesColor <- Grid.minorLinesColor
  } yield GraphicalContent(g => {
    val (majorH, majorV, minorH, minorV) = lines
    val (x, y, width, height) = dimensions

    val minorLines = new Path2D.Double

    minorH.foreach(y => { minorLines.moveTo(x, y); minorLines.lineTo(x + width, y) })
    minorV.foreach(x => { minorLines.moveTo(x, y); minorLines.lineTo(x, y + height) })

    val majorLines = new Path2D.Double

    majorH.foreach(y => { majorLines.moveTo(x, y); majorLines.lineTo(x + width, y) })
    majorV.foreach(x => { majorLines.moveTo(x, y); majorLines.lineTo(x, y + height) })

    g.setStroke(minorLinesStroke)
    g.setColor(minorLinesColor)
    g.draw(minorLines)

    g.setStroke(majorLinesStroke)
    g.setColor(majorLinesColor)
    g.draw(majorLines)
  })

  def snap: Expression[Double => Double] = for {
    majorInterval <- autoInterval;
    minorIntervalFactor <- Grid.minorIntervalFactor
  } yield {
    val interval = majorInterval * minorIntervalFactor

    x => Math.floor(x / interval + 0.5) * interval
  }
}

object Grid {
  val minorIntervalFactor = Variable.create(0.1)
  val intervalScaleFactor = Variable.create(2)

  val magThreshold = Variable.create(5)
  val minThreshold = Variable.create(2.5)

  val minorLinesStroke = Variable.create(new BasicStroke(1))
  val majorLinesStroke = Variable.create(new BasicStroke(1))

  val majorLinesColor = Variable.create(new Color(200, 200, 200))
  val minorLinesColor = Variable.create(new Color(240, 240, 240))
}