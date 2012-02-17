package org.workcraft.gui.modeleditor
import org.workcraft.dependencymanager.advanced.user.Variable
import java.awt.Color
import org.workcraft.scala.Expressions.Expression
import java.awt.Font

object CommonVisualSettings {
  val backgroundColor = Variable.create(Color.WHITE)
  val foregroundColor = Variable.create(Color.BLACK)
  val size = Variable.create(1.0)
  val strokeWidth = Variable.create(0.1)
  val iconSize = Variable.create(16)
  val fillColor = Variable.create(Color.WHITE)
  val serifFont = Variable.create(new Font("Serif", Font.PLAIN, 1))
  val sansSerifFont = Variable.create(new Font("SansSerif", Font.PLAIN, 1))  
}