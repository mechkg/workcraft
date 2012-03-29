package org.workcraft.gui.propertyeditor.bool


import org.workcraft.gui.propertyeditor.EditableProperty
import org.workcraft.scala.Expressions.Expression
import org.workcraft.scala.Expressions._


object BooleanProperty {
  def apply (name:String, expr:ModifiableExpression[Boolean]) = create(name, expr)
  def create(name:String, expr:ModifiableExpression[Boolean]): Expression[EditableProperty] = expr.map ( v => 
    EditableProperty (name, BooleanCellEditor, BooleanCellRenderer, v, expr.set(_:Boolean))
  )
}
