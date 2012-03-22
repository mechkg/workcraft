package org.workcraft.gui.modeleditor.tools

import java.awt.Color
import java.awt.Graphics2D
import java.awt.geom.Point2D
import org.workcraft.gui.modeleditor.MouseButton
import org.workcraft.gui.modeleditor.LeftButton
import org.workcraft.gui.modeleditor.Modifier
import org.workcraft.gui.modeleditor.ToolMouseListener
import org.workcraft.scala.Expressions._
import org.workcraft.graphics.GraphicalContent
import org.workcraft.gui.GUI
import javax.swing.JPanel
import org.workcraft.scala.effects.IO
import org.workcraft.scala.effects.IO._
import scalaz.Scalaz._

import org.workcraft.graphics.Colorisation

object NodeGeneratorTool {
  def apply(look: Button, painter: Expression[GraphicalContent], action: Point2D.Double => IO[Unit]): ModelEditorTool =
    new ModelEditorTool {
      def button = look
      def createInstance(env: ToolEnvironment) = ioPure.pure {
        new ModelEditorToolInstance {
          def keyBindings = Nil
          def mouseListener: Option[ToolMouseListener] = Some(new DummyMouseListener {
            override def buttonPressed(btn: MouseButton, modifiers: Set[Modifier], position: Point2D.Double): IO[Unit] = {
              if (btn == LeftButton) action(position) else {}.pure[IO]
            }
          })
          def userSpaceContent: Expression[GraphicalContent] = painter
          def screenSpaceContent: Expression[GraphicalContent] =
            GUI.editorMessage(env.viewport, Color.BLACK, "Click to create a " + look.label)
          def interfacePanel: Option[JPanel] = None
        }
      }
    }
}
