package org.workcraft.plugins.petrify
import org.workcraft.tasks.TaskControl
import org.workcraft.tasks.Task
import org.workcraft.scala.effects.IO._
import org.workcraft.scala.effects.IO
import scalaz.Scalaz._
import org.workcraft.tasks.ExternalProcess
import java.io.File

class WriteSgTask(command: String, input: File, output: File) extends Task[File, PetrifyError]{
  import PetrifyError._
  
  def runTask(tc: TaskControl) =
    tc.descriptionUpdate("Running petrify (write_sg)...") >>=|
      ExternalProcess.runSyncCollectOutput(List(command, input.getAbsolutePath, "-o", output.getAbsolutePath), None, tc.cancelRequest) >>= {
        case Left(cause) => ioPure.pure { Left(Some(CouldNotStart(cause))) }
        case Right((exitValue, cancelled, stdout, stderr)) => ioPure.pure {
          if (cancelled) Left(None) else exitValue match {
            case 0 => Right(output)
            case _ => Left(Some(RuntimeError(new String(stderr, "US-ASCII"))))
          }
        }
      }
}