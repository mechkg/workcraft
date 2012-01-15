import org.workcraft.scala.Util._
import org.workcraft.gui.propertyeditor.dubble.DoubleProperty
import org.workcraft.gui.propertyeditor.string.StringProperty
import pcollections.TreePVector
import org.workcraft.util.FieldAccessor
import org.workcraft.gui.propertyeditor.EditableProperty
import pcollections.PVector
import java.awt.geom.Point2D
import org.workcraft.scala.StorageManager
import org.workcraft.scala.Expressions.ModifiableExpression
import org.workcraft.graphics.LabelPositioning
import org.workcraft.gui.propertyeditor.choice.ChoiceProperty

package org.workcraft.plugins.cpog.scala.nodes {
  case class VisualProperties(label: ModifiableExpression[String], labelPositioning: ModifiableExpression[LabelPositioning], position: ModifiableExpression[Point2D.Double])

  object VisualProperties {
    def create(storage: StorageManager) = VisualProperties(storage.create(""), storage.create(LabelPositioning.BOTTOM), storage.create(new Point2D.Double(0, 0)))

    def getProperties(p: VisualProperties): PVector[EditableProperty] = {
      val xView = new FieldAccessor[Point2D.Double, java.lang.Double] {
        override def apply(arg: Point2D.Double) = arg.getX
        override def assign(old: Point2D.Double, x: java.lang.Double) = new Point2D.Double(x.doubleValue, old.getY)
      }

      val yView = new FieldAccessor[Point2D.Double, java.lang.Double] {
        override def apply(arg: Point2D.Double) = arg.getY
        override def assign(old: Point2D.Double, y: java.lang.Double) = new Point2D.Double(old.getX, y.doubleValue)
      }

      TreePVector.empty[EditableProperty] plus 
        StringProperty.create("label", p.label) plus 
        DoubleProperty.create("X", applyFieldAccessor(p.position, xView)) plus
        DoubleProperty.create("Y", applyFieldAccessor(p.position, yView)) plus
        ChoiceProperty.create("Label positioning", LabelPositioning.getChoice, p.labelPositioning)

    }
  }
}
