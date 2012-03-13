package org.workcraft.gui.propertyeditor.choice

import java.awt.Component
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression
import org.workcraft.gui.propertyeditor.EditableProperty
import org.workcraft.gui.propertyeditor.GenericCellEditor
import org.workcraft.gui.propertyeditor.GenericEditorProvider
import org.workcraft.gui.propertyeditor.RendererProvider
import org.workcraft.gui.propertyeditor.string.StringProperty
import org.workcraft.util.Action
import org.workcraft.util.Pair
import pcollections.PVector
import org.workcraft.scala.effects.IO
import org.workcraft.scala.Expressions.Expression
import org.workcraft.scala.Expressions._

object ChoiceProperty {

  def apply[T](name: String, choice: List[(String, T)], property: ModifiableExpression[T]): Expression[EditableProperty] = property.map(value => {

    val e = new GenericEditorProvider[T] {
      override def createEditor(initialValue: T, accept: IO[Unit], cancel: IO[Unit]): GenericCellEditor[T] =
        new ChoiceCellEditor[T](initialValue, choice, accept)
    }

    val r = new RendererProvider[T] {
      val m = choice.map(_.swap).toMap
      def createRenderer(value: T): Component = StringProperty.RendererProvider.createRenderer(m(value))
    }

    EditableProperty(name, e, r, value, property.set(_: T))
  })
}
