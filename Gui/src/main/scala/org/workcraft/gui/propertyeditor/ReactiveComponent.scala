package org.workcraft.gui.propertyeditor

import java.awt.Component
import org.workcraft.scala.Expressions._

trait ReactiveComponent {
  def component:Component
  def updateExpression:Expression[Unit]
}
