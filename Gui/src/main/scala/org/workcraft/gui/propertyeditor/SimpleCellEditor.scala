package org.workcraft.gui.propertyeditor

import java.awt.Component
import org.workcraft.scala.effects.IO

trait SimpleCellEditor {
  def commit: IO[Option[String]]
  def getComponent(): Component
}