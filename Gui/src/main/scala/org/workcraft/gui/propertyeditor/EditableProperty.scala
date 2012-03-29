package org.workcraft.gui.propertyeditor

import java.awt.BorderLayout
import java.awt.Component
import javax.swing.JPanel
import org.workcraft.scala.Expressions._
import org.workcraft.scala.effects.IO
import org.workcraft.scala.effects.IO._
import scalaz._
import Scalaz._
import javax.swing.JComponent

trait EditableProperty {
  val name: String
  def renderer(isSelected: Boolean, hasFocus: Boolean): JComponent
  def createEditor(accept: IO[Unit], cancel: IO[Unit]): SimpleCellEditor
}

object EditableProperty {
  def apply[T](propertyName: String, editor: GenericEditorProvider[T], _renderer: RendererProvider[T], value: T, _commit: T => IO[Unit]): EditableProperty = {
    withValidation(propertyName, editor, _renderer, value, (t: T) => ioPure.pure { try {_commit(t).unsafePerformIO; None } catch { case e: Throwable => Some(e.toString)}})
  }

  def withValidation[T](propertyName: String, editor: GenericEditorProvider[T], _renderer: RendererProvider[T], value: T, _commit: T => IO[Option[String]]): EditableProperty =
    new EditableProperty {
      override def renderer(isSelected: Boolean, hasFocus: Boolean) = {
        val panel = new JPanel
        panel.setLayout(new BorderLayout)
        panel.add(_renderer.createRenderer(value), BorderLayout.CENTER)
        panel
      }

      override def createEditor(accept: IO[Unit], cancel: IO[Unit]) = new SimpleCellEditor {
        val ge: GenericCellEditor[T] = editor.createEditor(value, accept, cancel)
        
        override def getComponent = ge.component
        override def commit = _commit( ge.getValue )
      }

      override val name = propertyName
    }
  
  def apply[T](propertyName: String, editor: GenericEditorProvider[T], renderer: RendererProvider[T], mewv : ModifiableExpressionWithValidation[T, String]) : Expression[EditableProperty] = {
    mewv.expr.map(value => {
      withValidation(propertyName, editor, renderer, value, (t:T) => mewv.set(t))
    })
  }
}
