package org.workcraft.plugins.lola

import org.workcraft.tasks.Task
import org.workcraft.scala.effects.IO._
import org.workcraft.scala.effects.IO
import org.workcraft.tasks.TaskControl
import scalaz.Scalaz._
import org.workcraft.gui.services.GuiTool
import org.workcraft.gui.services.ToolClass
import org.workcraft.gui.MainWindow
import org.workcraft.gui.tasks.ModalTaskDialog
import org.workcraft.gui.MainWindow
import org.workcraft.plugins.petri2.PetriNetService
import javax.swing.JOptionPane
import org.workcraft.plugins.petri2.PetriNet
import org.workcraft.tasks.ExternalProcess

trait LolaResult

trait LolaError

object LolaDeadlockTool extends GuiTool {
  val description = "Check for deadlocks using LoLA"
  val classification = ToolClass.Verification
  def run (mainWindow: MainWindow) = mainWindow.editorInFocus.expr.map(_.flatMap(_.content.model.implementation(PetriNetService)) match {
    case Some(pn) => Some(pn >>= (net => ModalTaskDialog.runTask(mainWindow, "Searching for deadlocks with LoLA", new LolaDeadlock) >>= (q => ioPure.pure {JOptionPane.showMessageDialog(mainWindow, q)})))
    case None => None
  })
}

class LolaDeadlock extends Task[PetriNet, String] {
    def runTask(tc: TaskControl) = 
      tc.descriptionUpdate("Starting LoLA...") >>=|
      
      ExternalProcess.run(List("ls"), None, new )
      
      ioPure.pure {

    def r: Either[Option[Int], Int] = {
      Range(1, 100).foreach(i => {
        if (tc.cancelRequest.unsafePerformIO)
          return Left(None)
        else {
          tc.progressUpdate(i / 1000.0).unsafePerformIO
          Thread.sleep(100)
        }
      })
      Right(8)
    }

    r
  }
}