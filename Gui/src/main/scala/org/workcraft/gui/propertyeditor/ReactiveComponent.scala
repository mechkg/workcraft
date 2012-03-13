package org.workcraft.gui.propertyeditor

import java.awt.Component
import org.workcraft.scala.Expressions._
import javax.swing.JPanel
import java.awt.BorderLayout

class ReactiveComponent(component: Component, updateExpression: AutoRefreshHandle) extends JPanel(new BorderLayout) {
  add(component, BorderLayout.CENTER)
}

object ReactiveComponent {
  def apply(component: Component, updateExpression: AutoRefreshHandle) = new ReactiveComponent(component, updateExpression)
}