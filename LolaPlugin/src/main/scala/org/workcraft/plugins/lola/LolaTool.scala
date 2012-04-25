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
import org.workcraft.gui.modeleditor.ModelEditorPanel
import org.workcraft.plugins.petri2.PetriNetService
import javax.swing.JOptionPane
import org.workcraft.plugins.petri2.PetriNet
import org.workcraft.tasks.ExternalProcess
import java.io.File
import org.workcraft.tasks.DiscardingSynchronousListener
import org.workcraft.gui.docking.DockableWindow

import org.workcraft.services.ExportError
import org.workcraft.gui.modeleditor.ShowTraceService

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

sealed trait LolaResult

object LolaResult {
  case class Positive (trace: List[String]) extends LolaResult
  case class Negative (trace: List[String]) extends LolaResult
}

class LolaVerificationTool(val lolaCommand: String, 
			   val description: String, 
			   val resultHandler: (LolaResult, MainWindow, DockableWindow[ModelEditorPanel]) => IO[Unit]) extends GuiTool {
  import LolaError._
  import LolaChainError._

  val classification = ToolClass.Verification

  def run(mainWindow: MainWindow) = mainWindow.editorInFocus.expr.map(editorWindow => editorWindow.flatMap(_.content.model.implementation(PetriNetService)) match {
    case Some(pn) => Some({

      val input = File.createTempFile("workcraft", ".lola")
      val output = File.createTempFile("workcraft", ".lolapath")

      val exportTask = new LolaExportJob(pn).asTask(input).mapError2(LolaChainError.LolaExportError(_))
      val lolaTask = new LolaTask(lolaCommand, input, output).mapError2(LolaChainError.LolaRunError(_))

      val megaTask = exportTask flatMap (_ => lolaTask)

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
	case Right(result) => resultHandler(result, mainWindow, editorWindow.get)
      }
    })
    
    case None => None
  })
}

class LolaTask(lolaCommand: String, input: File, output: File) extends Task[LolaResult, LolaError] {
  def loadTrace: List[String] = if (output.exists) {
    scala.io.Source.fromFile(output).getLines.toList match {
      case Nil => Nil
      case x :: xs => xs
    }
  } else Nil

  def runTask(tc: TaskControl) =
    tc.descriptionUpdate("Running LoLA...") >>=|
  ExternalProcess.runSyncCollectOutput(List(lolaCommand, input.getAbsolutePath, "-p", output.getAbsolutePath), None, tc.cancelRequest) >>= {
    case Left(cause) => ioPure.pure { Left(Some(LolaError.CouldNotStart(cause))) }
    case Right((exitValue, cancelled, stdout, stderr)) => ioPure.pure {
      if (cancelled) Left(None) else exitValue match {
	// 0 = specified state or deadlock found/net or place unbounded/home marking exists/net is reversible/predicate is live etc.
	// 1 = the opposite verification result as a thumb rule, if the outcome of a verification result can be supported by a counterexample or witness path, that case corresponds to return value 0
        case 0 => Right(LolaResult.Positive(loadTrace))
        case 1 => Right(LolaResult.Negative(loadTrace))
        case 2 => Left(Some(LolaError.OutOfMemory))
        case 3 => Left(Some(LolaError.Syntax(new String(stderr, "US-ASCII"))))
        case 4 => Left(Some(LolaError.ArgsOrIO(new String(stderr, "US-ASCII"))))
        case 5 => Left(Some(LolaError.StateOverflow))
        case _ => Left(Some(LolaError.Undefined(new String(stderr, "US-ASCII"))))
      }
    }
  }
}
