package org.workcraft.plugins.cpog.scala
import java.awt.geom.Point2D
import nodes._
import org.workcraft.scala.Expressions._
import org.workcraft.dom.visual.BoundedColorisableGraphicalContent
import org.workcraft.dom.visual.Touchable
import org.workcraft.graphics.RichGraphicalContent

class ControlPoint (val position: ModifiableExpression[Point2D.Double], val graphics:Expression[RichGraphicalContent])