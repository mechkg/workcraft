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


object ChoiceProperty {
  
  def apply[T](name:String, choice:List[(String,T)], property:ModifiableExpression[T]):EditableProperty = {
    
    val e = new GenericEditorProvider[T] {
			override def createEditor(initialValue : T, accept : IO[Unit], cancel : IO[Unit]) : GenericCellEditor[T] =
				new ChoiceCellEditor[T](initialValue, choice, accept)
    }
    
    val r = new RendererProvider[T] {
      val m = choice.map{case (a,b) => (b,a)}.toMap
      def createRenderer(value:T):Component = StringProperty.RendererProvider.createRenderer(m(value))
    }
    
    return EditableProperty(name, e, r, property)
  }
}
