package org.workcraft.services
import java.io.OutputStream
import org.workcraft.scala.effects.IO
import org.workcraft.scala.effects.IO._
import scalaz._
import Scalaz._
import java.io.File
import java.io.FileOutputStream
import org.workcraft.tasks.Task

object ExporterService extends GlobalService[Exporter]

case class ServiceNotAvailableException(service: ModelService[_]) {
  override def toString = "Model service is not available: " + service.getClass.getSimpleName
}

trait Exporter {
  val targetFormat: Format
  def export(model: ModelServiceProvider): Either[ServiceNotAvailableException, ExportJob]
}

trait ExportJob {
  def job(stream: File): IO[Option[Throwable]] 
}

class FileExportJob (val file: File, exportJob: ExportJob) {
  val job = exportJob.job(file) 
} 

object FileExportJob {
  def apply (file: File, exportJob: ExportJob) = new FileExportJob (file, exportJob)
}