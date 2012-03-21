package org.workcraft.services
import java.io.OutputStream
import org.workcraft.scala.effects.IO
import org.workcraft.scala.effects.IO._
import scalaz._
import Scalaz._
import java.io.File
import java.io.FileOutputStream
import org.workcraft.tasks.Task
import org.workcraft.tasks.TaskControl

object ExporterService extends GlobalService[Exporter]

case class ServiceNotAvailableException(service: ModelService[_]) {
  override def toString = "Model service is not available: " + service.getClass.getSimpleName
}

trait Exporter {
  val targetFormat: Format
  def export(model: ModelServiceProvider): Either[ServiceNotAvailableException, ExportJob]
}

sealed trait ExportError

object ExportError {
  case class Exception (exception: Throwable) extends ExportError
  case class Message (message: String) extends ExportError
}

trait ExportJob {
  def job(stream: File): IO[Option[ExportError]]
  
  def asTask(file: File) = new Task[File, ExportError] {
    def runTask(tc: TaskControl) = tc.descriptionUpdate ("Exporting " + file.getPath) >>=| (job(file) map {case None => Right(file); case Some(error) => Left(Some(error))})
  } 
}