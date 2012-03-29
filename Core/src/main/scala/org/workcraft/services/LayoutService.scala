package org.workcraft.services

import java.awt.geom.Point2D
import org.workcraft.scala.effects.IO
import java.awt.geom.Rectangle2D
import java.awt.geom.AffineTransform

object LayoutService extends Service[ModelScope, IO[Layout]]

sealed abstract class LayoutOrientation (val transform: AffineTransform)

object LayoutOrientation {
  case object Up extends LayoutOrientation(new AffineTransform())
  case object Down extends LayoutOrientation (AffineTransform.getScaleInstance(1, -1))
  case object LeftToRight extends LayoutOrientation (AffineTransform.getRotateInstance(scala.math.Pi / 2))
  case class Custom (t: AffineTransform) extends LayoutOrientation(t) 
}

trait LayoutNode

case class Layout (spec: LayoutSpec, apply: List[(LayoutNode, Point2D.Double)] => IO[Unit])

case class LayoutSpec (
    nodes: List[LayoutNode], 
    size: LayoutNode => (Double, Double),  // (width, height)
    outgoingArcs: LayoutNode => List[LayoutNode],
    nodeSeparation: Double,
    rankSeparation: Double,
    orientation: LayoutOrientation)