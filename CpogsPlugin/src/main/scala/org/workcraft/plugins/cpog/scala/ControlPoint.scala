package org.workcraft.plugins.cpog.scala
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression
import java.awt.geom.Point2D
import nodes._
import org.workcraft.dependencymanager.advanced.core.Expression
import org.workcraft.dom.visual.BoundedColorisableGraphicalContent
import org.workcraft.dom.visual.Touchable
import org.workcraft.graphics.RichGraphicalContent

class ControlPoint (val position: ModifiableExpression[Point2D], val graphics:Expression[RichGraphicalContent])