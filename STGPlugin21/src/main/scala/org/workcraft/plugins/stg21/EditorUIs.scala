package org.workcraft.plugins.stg21
import org.workcraft.gui.propertyeditor.EditableProperty
import org.workcraft.scala.Expressions.ModifiableExpression
import org.workcraft.gui.propertyeditor.string.StringProperty
import org.workcraft.gui.propertyeditor.integer.IntegerProperty
import org.workcraft.gui.propertyeditor.bool.BooleanProperty
import org.workcraft.gui.propertyeditor.dubble.DoubleProperty
import org.workcraft.plugins.stg21.modifiable._
import org.workcraft.gui.propertyeditor.choice.ChoiceProperty
import scala.collection.JavaConversions._
import pcollections.TreePVector

object EditorUIs {
  case class EditorUI[T](create : String => ModifiableExpression[T] => EditableProperty);
  implicit val stringEditorUI : EditorUI[String] = EditorUI (name => x => StringProperty.create(name, x))
  implicit val integerEditorUI : EditorUI[Integer] = EditorUI (name => x => IntegerProperty.create(name, x))
  implicit val intEditorUI : EditorUI[Int] = EditorUI (name => x => IntegerProperty.create(name, x.applyIso[Integer](x => x, x => x)))
  implicit val doubleEditorUI : EditorUI[java.lang.Double] = EditorUI (name => x => DoubleProperty.create(name, x))
  implicit val valueDoubleEditorUI : EditorUI[Double] = 
    EditorUI (name => x => DoubleProperty.create(name, x.applyIso[java.lang.Double](x => x, x => x)))
  case class Choice[T] (values : List[(String, T)])
  implicit def choiceEditorUI[T] (implicit choice : Choice[T]) : EditorUI[T] = EditorUI { name => x =>
    ChoiceProperty.create(name, TreePVector.from[org.workcraft.util.Pair[String, T]](choice.values.map(p => org.workcraft.util.Pair.of(p._1,p._2))), x)
  }
}
