package org.workcraft.gui.propertyeditor

import java.awt.BorderLayout
import java.awt.Component
import javax.swing.JPanel
import org.workcraft.scala.Expressions._
import org.workcraft.scala.effects.IO
import org.workcraft.scala.effects.IO._
import scalaz._
import Scalaz._

trait EditableProperty {
  def name: String
  def renderer(isSelected: Expression[Boolean], hasFocus: Expression[Boolean]): ReactiveComponent
  def editorMaker(): Expression[EditorProvider]
}
object EditableProperty {
  def apply[T](propertyName: String, editor: GenericEditorProvider[T], _renderer: RendererProvider[T], property: ModifiableExpression[T]): EditableProperty =
    new EditableProperty {
      override def renderer(isSelected: Expression[Boolean], hasFocus: Expression[Boolean]) = {
        val panel = new JPanel
        new ReactiveComponent {
          override def updateExpression = {
            property map (value => {
              panel.removeAll
              panel.setLayout(new BorderLayout)
              panel.add(_renderer.createRenderer(value), BorderLayout.CENTER)
            })
          }
          override def component = panel
        }
      }
      override def editorMaker = property.map(value => {
        new EditorProvider {
          override def getEditor(close: IO[Unit]) = new SimpleCellEditor {
            val ge: GenericCellEditor[T] = editor.createEditor(value, ioPure.pure { commit } >>=| close, close)

            override def getComponent = ge.component
            override def commit = property.set(ge.getValue).unsafePerformIO
          }
        }
      })
      override def name = propertyName
    }
}
