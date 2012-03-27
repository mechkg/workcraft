package org.workcraft.services

import java.awt.geom.Point2D
import org.workcraft.scala.effects.IO
import java.awt.geom.Rectangle2D

object LayoutService extends Service[ModelScope, IO[Layout]]

trait LayoutNode

case class Layout (spec: LayoutSpec, apply: List[(LayoutNode, Point2D.Double)] => IO[Unit])

case class LayoutSpec (
    nodes: List[LayoutNode], 
    size: LayoutNode => (Double, Double),  // (width, height)
    outgoingArcs: LayoutNode => List[LayoutNode]) 