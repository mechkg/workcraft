package org.workcraft.gui.tasks

import org.workcraft.tasks.Task
import org.workcraft.scala.effects.IO
import javax.swing.JOptionPane
import org.workcraft.tasks.TaskControl
import org.workcraft.scala.Expressions._
import scalaz.Scalaz._
import java.awt.Window

object ModalTaskDialog {


  def runTask[O, E](parentWindow: Window, task: Task[O, E]): IO[Either[Option[E], O]] = 
    (newVar[Boolean](false) <***> (newVar[Option[Double]](None), newVar[String]("Hohoho")))( (cancelRequested, progress, description) => { 
    JOptionPane.showMessageDialog(parentWindow, new TaskPanel(progress, description, cancelRequested.set(true)))
    Right (8)
    })
}