package org.workcraft.gui.propertyeditor.integer

import org.workcraft.gui.propertyeditor.EditableProperty
import org.workcraft.gui.propertyeditor.string.StringProperty
import org.workcraft.scala.Expressions._

import org.workcraft.scala.Expressions._
object IntegerProperty {
  
  def apply(name : String, property : ModifiableExpressionWithValidation[Int, String]) : Expression[EditableProperty] =
    StringProperty(name, property.xmap(_.toString)(s => try { Right(Integer.parseInt(s)) } catch {case e: Throwable => Left (e.toString)}))
  
  def apply(name:String, property:ModifiableExpression[Int]): Expression[EditableProperty] =
    StringProperty(name, property.xmap(_.toString)(Integer.parseInt(_)))
}
