package org.workcraft.gui.modeleditor
import org.workcraft.scala.Expressions.Expression
import org.workcraft.graphics.GraphicalContent

class Grid (val viewport: Viewport) {
  def graphicalContent : Expression[GraphicalContent]
}