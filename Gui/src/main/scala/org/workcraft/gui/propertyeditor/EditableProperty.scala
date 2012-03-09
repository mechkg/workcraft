package org.workcraft.gui.propertyeditor

import java.awt.BorderLayout
import java.awt.Component
import javax.swing.JPanel
import org.workcraft.dependencymanager.advanced.core.Expression
import org.workcraft.dependencymanager.advanced.core.Expressions
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression
import org.workcraft.util.Action
import org.workcraft.util.Function
import org.workcraft.util.Nothing


trait EditableProperty {
  def name():String
  def renderer(isSelected:Expression[Boolean], hasFocus:Expression[Boolean]):ReactiveComponent
  def editorMaker():Expression[]object Util {
    def create(propertyName:String, editor:GenericEditorProvider[T], renderer:RendererProvider[T], property:ModifiableExpression[T]):EditableProperty = {
      return new EditableProperty()
    }

  }
}
