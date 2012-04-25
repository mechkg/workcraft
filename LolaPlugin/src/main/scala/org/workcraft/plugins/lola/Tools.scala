package org.workcraft.plugins.lola

import org.workcraft.tasks.Task
import org.workcraft.scala.effects.IO._
import org.workcraft.scala.effects.IO
import org.workcraft.tasks.TaskControl
import scalaz.Scalaz._
import org.workcraft.gui.services.GuiTool
import org.workcraft.gui.services.ToolClass
import org.workcraft.gui.MainWindow
import org.workcraft.gui.docking.DockableWindow
import org.workcraft.gui.tasks.ModalTaskDialog
import org.workcraft.gui.MainWindow
import org.workcraft.plugins.petri2.PetriNetService
import javax.swing.JOptionPane
import org.workcraft.plugins.petri2.PetriNet
import org.workcraft.tasks.ExternalProcess
import java.io.File
import org.workcraft.tasks.DiscardingSynchronousListener
import org.workcraft.gui.modeleditor.ModelEditorPanel

import org.workcraft.services.ExportError
import org.workcraft.gui.modeleditor.ShowTraceService

object LolaDeadlockTool extends LolaVerificationTool (
  "./tools/lola-deadlock",
  "Check for deadlocks using LoLA",
  LolaVerificationResult.handleDeadlockResult(_,_,_))

object LolaReversibilityTool extends LolaVerificationTool (
  "./tools/lola-reversibility", 
  "Check reversibility using LoLA",  
  LolaVerificationResult.handleReversibilityResult ( _,_,_))

  
object LolaVerificationResult {
  import LolaResult._

  def showTrace (mainWindow: MainWindow, editorWindow: DockableWindow[ModelEditorPanel], trace: List[String]): IO[Unit] =
    editorWindow.content.editor.implementation(ShowTraceService) match {
      case Some(service) => service.show(trace) match {	case (tool, instance) => 
	  mainWindow.setFocus(Some(editorWindow)) >>=| editorWindow.content.toolbox.selectToolWithInstance(tool, instance) }
      case None => ioPure.pure { JOptionPane.showMessageDialog(mainWindow, "Unfortunately, this model does not support interactive trace replays :-(\n\nWitness trace: " + trace.mkString(", "), "Verification result", JOptionPane.INFORMATION_MESSAGE) }
    }


  def handleDeadlockResult (result: LolaResult, mainWindow: MainWindow, editorWindow: DockableWindow[ModelEditorPanel]) = result match {
    case Positive(trace) => ioPure.pure {
      JOptionPane.showConfirmDialog(mainWindow,
				    "The net has a deadlock state!\nWould you like to examine the event trace that leads to the deadlock?",
				    "Deadlock verification result", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE) == JOptionPane.YES_OPTION
    } >>= (if (_) showTrace(mainWindow, editorWindow, trace) else IO.Empty)
    case Negative(_) => ioPure.pure { JOptionPane.showMessageDialog(mainWindow, "The net is deadlock free.") }
  }

def handleReversibilityResult (result: LolaResult, mainWindow: MainWindow, editorWindow: DockableWindow[ModelEditorPanel]) = result match {
    case Positive(_) => ioPure.pure { JOptionPane.showMessageDialog(mainWindow, "The net is reversible.") }
    case Negative(trace) => ioPure.pure {
      JOptionPane.showConfirmDialog(mainWindow,
				    "There is an irreversible state.\nWould you like to examine the event trace that leads to that state?",
				    "Reversibility verification result", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE) == JOptionPane.YES_OPTION
    } >>= (if (_) showTrace(mainWindow, editorWindow, trace) else IO.Empty) 
  }

}
