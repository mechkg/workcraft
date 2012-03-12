package org.workcraft.services
import java.io.OutputStream
import org.workcraft.scala.effects.IO

object ExporterService extends GlobalService[Exporter]

case class ServiceNotAvailableException(service: ModelService[_])

trait Exporter {
  val targetFormat: Format
  def export(model: ModelServiceProvider): Either[ServiceNotAvailableException, ExportJob]
}

trait ExportJob {
  val complete: Boolean // true if no information is lost when exporting
  def job(stream: OutputStream): IO[Unit]
}