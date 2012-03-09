package org.workcraft.gui.propertyeditor

import java.awt.Component
import org.workcraft.dependencymanager.advanced.core.Expression
import org.workcraft.util.Nothing


trait ReactiveComponent {
  def component():Component
  def updateExpression():Expression[]
}
