package org.workcraft.services
import java.io.OutputStream
import org.workcraft.scala.effects.IO
import org.workcraft.scala.effects.IO._
import scalaz._
import Scalaz._
import java.io.File
import java.io.FileOutputStream

object ExporterService extends GlobalService[Exporter]

case class ServiceNotAvailableException(service: ModelService[_]) {
  override def toString = "Model service is not available: " + service.getClass.getSimpleName
}

trait Exporter {
  val targetFormat: Format
  def export(model: ModelServiceProvider): Either[ServiceNotAvailableException, ExportJob]
}

trait ExportJob {
  def job(stream: OutputStream): IO[Unit]
}

class FileExportJob (val file: File, exportJob: ExportJob) {
  val job = ioPure.pure {new FileOutputStream(file)} >>= (os => exportJob.job(os) >>=| ioPure.pure {os.close()})
} 

object FileExportJob {
  def apply (file: File, exportJob: ExportJob) = new FileExportJob (file, exportJob)
}