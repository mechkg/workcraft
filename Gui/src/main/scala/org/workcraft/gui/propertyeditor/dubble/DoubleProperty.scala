package org.workcraft.gui.propertyeditor.dubble

import org.workcraft.gui.propertyeditor.EditableProperty
import org.workcraft.gui.propertyeditor.string.StringProperty
import org.workcraft.scala.Expressions._
import org.workcraft.scala.Expressions._

object DoubleProperty {
  def apply(name: String, property: ModifiableExpression[Double]): Expression[EditableProperty] = {
    return StringProperty(name, property.xmap(_.formatted("%.2f"))(java.lang.Double.parseDouble(_)))
  }
}
