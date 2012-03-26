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
import java.io.File
import org.workcraft.tasks.DiscardingSynchronousListener
import org.workcraft.tasks.Task._
import org.workcraft.services.ExportError
import org.workcraft.gui.modeleditor.ShowTraceService

sealed trait LolaDeadlockResult

object LolaDeadlockResult {
  case object Found extends LolaDeadlockResult // 0   specified state or deadlock found/net or place unbounded/home marking exists/net is reversible/predicate is live etc.
  case object NotFound extends LolaDeadlockResult // 1 the opposite verification result as a thumb rule, if the outcome of a verification result can be supported by a counterexample or witness
  // path, that case corresponds to return value 0
}

sealed trait LolaError

object LolaError {
  case class CouldNotStart(cause: Throwable) extends LolaError
  case object OutOfMemory extends LolaError // 2 Memory overflow during verification
  case class Syntax(reason: String) extends LolaError // 3 Syntax error in the net or property description
  case class ArgsOrIO(reason: String) extends LolaError // 4 Error in accessing files (cannot open, no write permission etc.) or invalid command line parameters.
  case object StateOverflow extends LolaError // 5 Maximal number of states (MAXIMALSTATES in userconfig.H) exceeded
  case class Undefined(reason: String) extends LolaError
}

sealed trait LolaChainError

object LolaChainError {
  case class LolaExportError(error: ExportError) extends LolaChainError
  case class LolaRunError(error: LolaError) extends LolaChainError
}

object LolaDeadlockTool extends GuiTool {
  import LolaDeadlockResult._
  import LolaError._
  import LolaChainError._

  val description = "Check for deadlocks using LoLA"
  val classification = ToolClass.Verification

  def run(mainWindow: MainWindow) = mainWindow.editorInFocus.expr.map(editorWindow => editorWindow.flatMap(_.content.model.implementation(PetriNetService)) match {
    case Some(pn) => Some({

      val input = File.createTempFile("workcraft", ".lola")
      val output = File.createTempFile("workcraft", ".lolapath")

      val exportTask = new LolaExportJob(pn).asTask(input).mapError2(LolaChainError.LolaExportError(_))
      val deadlockTask = new LolaDeadlockTask("e:/lola-1.16/src/lola-deadlock", input, output).mapError2(LolaChainError.LolaRunError(_))

      val megaTask = exportTask flatMap (_ => deadlockTask)

      ModalTaskDialog.runTask(mainWindow, "Searching for deadlocks with LoLA", megaTask) >>= {
        case Left(None) => ioPure.pure { JOptionPane.showMessageDialog(mainWindow, "Cancelled") }
        case Left(Some(error)) => error match {
          case LolaExportError(ExportError.Exception(reason)) => ioPure.pure { JOptionPane.showMessageDialog(mainWindow, "Error while exporting the net in LoLA format:\n" + reason.toString(), "Error", JOptionPane.ERROR_MESSAGE) }
          case LolaExportError(ExportError.Message(message)) => ioPure.pure { JOptionPane.showMessageDialog(mainWindow, "Error while exporting the net in LoLA format:\n" + message, "Error", JOptionPane.ERROR_MESSAGE) }
          case LolaRunError(error) => error match {
            case CouldNotStart(reason) => ioPure.pure { JOptionPane.showMessageDialog(mainWindow, "Could not start LoLA:\n" + reason.toString(), "Error", JOptionPane.ERROR_MESSAGE) }
            case OutOfMemory => ioPure.pure { JOptionPane.showMessageDialog(mainWindow, "LoLA ran out of memory", "Error", JOptionPane.ERROR_MESSAGE) }
            case Syntax(reason) => ioPure.pure { JOptionPane.showMessageDialog(mainWindow, "Synax error in LoLA input (this is very likely a bug in Workcraft):\n" + reason, "Error", JOptionPane.ERROR_MESSAGE) }
            case ArgsOrIO(reason) => ioPure.pure { JOptionPane.showMessageDialog(mainWindow, "Invalid arguments or IO error in LoLA:\n(this means either a bug in Workcraft, file permissions issue or disk being full)\n" + reason, "Error", JOptionPane.ERROR_MESSAGE) }
            case StateOverflow => ioPure.pure { JOptionPane.showMessageDialog(mainWindow, "LoLA exceeded the state space limit.\nThis net's state space is too large to be analysed by LoLA.", "Error", JOptionPane.ERROR_MESSAGE) }
            case Undefined(reason) => ioPure.pure { JOptionPane.showMessageDialog(mainWindow, "Unhandled exception in LoLA:\n" + reason, "Error", JOptionPane.ERROR_MESSAGE) }
          }
        }
        case Right(Found) => ioPure.pure { scala.io.Source.fromFile(output).getLines.toList.tail } >>= { trace =>
          editorWindow.flatMap(_.content.editor.implementation(ShowTraceService)) match {
            case Some(service) => {
              ioPure.pure {
                JOptionPane.showConfirmDialog(mainWindow, "The net has a deadlock state!\nWould you like to examine the event trace that leads to the deadlock state?",
                  "Verification result", JOptionPane.YES_NO_OPTION, JOptionPane.INFORMATION_MESSAGE) == JOptionPane.YES_OPTION
              } >>= (if (_) service.show(trace) match {
                case (tool, instance) => {
                  val window = editorWindow.get
                  mainWindow.setFocus(Some(window)) >>=| window.content.toolbox.selectToolWithInstance(tool, instance)
                }
              }
              else IO.Empty)
            }

            case None => ioPure.pure { JOptionPane.showMessageDialog(mainWindow, "The net has a deadlock!\n\nUnfortunately, this model does not support interactive trace replays :-(\n\nWitness trace: " + trace.mkString(", "), "Verification result", JOptionPane.INFORMATION_MESSAGE) }
          }
        }
        case Right(NotFound) => ioPure.pure { JOptionPane.showMessageDialog(mainWindow, "The net is deadlock free.") }
      }
    })
    // TODO: Delete temp files

    case None => None
  })
}

class LolaDeadlockTask(lolaCommand: String, input: File, output: File) extends Task[LolaDeadlockResult, LolaError] {
  def runTask(tc: TaskControl) =
    tc.descriptionUpdate("Running LoLA...") >>=|
      ExternalProcess.runSyncCollectOutput(List(lolaCommand, input.getAbsolutePath, "-p", output.getAbsolutePath), None, tc.cancelRequest) >>= {
        case Left(cause) => ioPure.pure { Left(Some(LolaError.CouldNotStart(cause))) }
        case Right((exitValue, cancelled, stdout, stderr)) => ioPure.pure {
          if (cancelled) Left(None) else exitValue match {
            case 0 => Right(LolaDeadlockResult.Found)
            case 1 => Right(LolaDeadlockResult.NotFound)
            case 2 => Left(Some(LolaError.OutOfMemory))
            case 3 => Left(Some(LolaError.Syntax(new String(stderr, "US-ASCII"))))
            case 4 => Left(Some(LolaError.ArgsOrIO(new String(stderr, "US-ASCII"))))
            case 5 => Left(Some(LolaError.StateOverflow))
            case _ => Left(Some(LolaError.Undefined(new String(stderr, "US-ASCII"))))
          }
        }
      }
}