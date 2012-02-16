package org.workcraft.gui.modeleditor
import org.workcraft.dependencymanager.advanced.user.Variable
import java.awt.Color
import org.workcraft.scala.Expressions.Expression

object CommonVisualSettings {
  val backgroundColorVar = Variable.create(Color.WHITE)
  val foregroundColorVar = Variable.create(Color.BLACK)
  
  def backgroundColor: Expression[Color] = backgroundColorVar
  def foregroundColor: Expression[Color] = foregroundColorVar
}