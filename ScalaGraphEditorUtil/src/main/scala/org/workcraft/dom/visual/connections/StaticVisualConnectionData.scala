package org.workcraft.dom.visual.connections
import java.awt.geom.Point2D

sealed trait StaticVisualConnectionData

case class Bezier(cp1 : RelativePoint, cp2 : RelativePoint) extends StaticVisualConnectionData

case class Polyline(cps : List[Point2D.Double]) extends StaticVisualConnectionData
