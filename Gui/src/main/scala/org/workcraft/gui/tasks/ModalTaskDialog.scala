package org.workcraft.gui.tasks

import org.workcraft.tasks.Task
import org.workcraft.scala.effects.IO
import org.workcraft.scala.effects.IO._
import javax.swing.JOptionPane
import org.workcraft.tasks.TaskControl
import org.workcraft.scala.Expressions._
import scalaz.Scalaz._
import java.awt.Window
import javax.swing.JDialog
import org.workcraft.gui.GUI
import javax.swing.SwingUtilities

object ModalTaskDialog {
  def runTask[O, E](parentWindow: Window, title: String, task: Task[O, E]): IO[Either[Option[E], O]] =
    (newVar[Boolean](false) <***> (newVar[Option[Double]](None), newVar[String]("Please wait...")))((cancelRequested, progress, description) => {

      val dialog = new JDialog
      var result: Either[Option[E], O] = null

      task.runAsynchronously(TaskControl(
        cancelRequested.eval, p => progress.set(Some(p)), description.set(_)),
        (res: Either[Option[E], O]) => ioPure.pure { SwingUtilities.invokeLater { new Runnable { def run = { result = res; dialog.setVisible(false) } } } }).unsafePerformIO

      dialog.setTitle(title)
      dialog.setContentPane(new TaskPanel(progress, description, cancelRequested.set(true)))
      dialog.pack()

      GUI.centerToParent(dialog, parentWindow)

      dialog.setModal(true)
      dialog.setVisible(true)

      result
    })
}