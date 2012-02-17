package org.workcraft.gui.modeleditor

import org.workcraft.scala.Expressions._
import scalaz._
import Scalaz._
import org.workcraft.graphics.GraphicalContent
import java.awt.geom.AffineTransform
import java.awt.geom.Rectangle2D
import java.awt.geom.Point2D
import org.workcraft.dependencymanager.advanced.user.Variable
import org.workcraft.dependencymanager.advanced.core.GlobalCache

class Viewport(val dimensions: Expression[(Int, Int, Int, Int)]) {
  val translationX = Variable.create (0.0)
  val translationY = Variable.create (0.0)
  val scale = Variable.create (0.0625)
  val origin = new Point2D.Double(0.0, 0.0)
  
  def transform: Expression[AffineTransform] = for {
    view <- viewTransform;
    proj <- projection
  } yield {
    val result = new AffineTransform(proj)
    result.concatenate(view)
    result
  }
  
  def inverseTransform: Expression[AffineTransform] = transform.map(_.createInverse)
  
  def projection = dimensions.map { case (x, y, w, h) => {
    val result = new AffineTransform()
    result.translate(w/2 + x, h/2 + y)
    if (h != 0)
      result.scale(h/2, h/2)
    result
  }} 
  
  def viewTransform = for {
    tx <- translationX;
    ty <- translationY;
    scale <- scale
  } yield {
    val result = new AffineTransform()
    result.scale(scale, scale)
    result.translate(tx, ty)
    result
  }

  def screenToUser: Expression[Point2D => Point2D] = for {
    inverseTransform <- inverseTransform
  } yield p => {
    val result = new Point2D.Double
    inverseTransform.transform(p, result)
    result
  }

  def screenToUserX: Expression[Int => Double] = for {
    screenToUser <- screenToUser
  } yield x => screenToUser(new Point2D.Double(x, 0)).getX

  def screenToUserY: Expression[Int => Double] = for {
    screenToUser <- screenToUser
  } yield y => screenToUser(new Point2D.Double(0, y)).getY

  def userToScreen: Expression[Point2D => Point2D] = for {
    transform <- transform
  } yield p => {
    val result = new Point2D.Double
    transform.transform(p, result)
    result
  }

  def userToScreenX: Expression[Double => Int] = for {
    userToScreen <- userToScreen
  } yield x => userToScreen(new Point2D.Double(x, 0)).getX.toInt

  def userToScreenY: Expression[Double => Int] = for {
    userToScreen <- userToScreen
  } yield y => userToScreen(new Point2D.Double(0, y)).getY.toInt

  def visibleArea: Expression[Rectangle2D] = for {
    inverseTransform <- inverseTransform;
    viewportDimensions <- dimensions
  } yield {
    val (viewX, viewY, viewWidth, viewHeight) = viewportDimensions

    val viewLL = new Point2D.Double(viewX, viewHeight + viewY)
    val viewUR = new Point2D.Double(viewWidth + viewX, viewY)

    val visibleUL = new Point2D.Double
    val visibleLR = new Point2D.Double

    inverseTransform.transform(viewLL, visibleUL)
    inverseTransform.transform(viewUR, visibleLR)

    new Rectangle2D.Double(visibleUL.getX, visibleLR.getY, visibleLR.getX - visibleUL.getX, visibleUL.getY - visibleLR.getY)
  }
  
  def pan (dx: Int, dy: Int) = {
    // Scary IO magic
    val userToScreenf = GlobalCache.eval(userToScreen.jexpr)
    val screenToUserf = GlobalCache.eval(screenToUser.jexpr)
    val tx = GlobalCache.eval(translationX.jexpr)
    val ty = GlobalCache.eval(translationY.jexpr)
    
    val originInScreenSpace = userToScreenf(origin)
    val panInScreenSpace = new Point2D.Double (originInScreenSpace.getX + dx, originInScreenSpace.getY + dy)
    val panInUserSpace = screenToUserf(panInScreenSpace)
    
    translationX.setValue(tx + panInUserSpace.getX)
    translationY.setValue(ty + panInUserSpace.getY)
  }
  
  def zoom (levels: Int) = {
    // More IO
    val curScale = GlobalCache.eval(scale.jexpr)
    val newScale = curScale * Math.pow (Viewport.scaleFactor, levels)
    
    scale.setValue(Math.min(Math.max (newScale, 0.01), 1.0))
  }
  
  def zoomTo (levels: Int, anchor: Point2D) = {
    val screenToUserf = GlobalCache.eval(screenToUser.jexpr)
    val tx = GlobalCache.eval(translationX.jexpr)
    val ty = GlobalCache.eval(translationY.jexpr)
    
    val anchorInUserSpace = screenToUserf (anchor)
    zoom (levels)
    val anchorInNewSpace = screenToUserf (anchor)
    
    translationX.setValue (tx + anchorInNewSpace.getX - anchorInUserSpace.getX)
    translationY.setValue (ty + anchorInNewSpace.getY - anchorInUserSpace.getY)
  }
}

object Viewport {
  val scaleFactor = Math.pow(2, 0.125)   
}