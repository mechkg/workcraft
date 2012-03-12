package org.workcraft.plugins.petri2
import org.workcraft.services.Exporter
import org.workcraft.services.Format
import org.workcraft.services.ModelServiceProvider
import org.workcraft.services.ServiceNotAvailableException
import org.workcraft.services.ExportJob
import java.io.OutputStream
import org.workcraft.scala.effects.IO
import org.workcraft.scala.effects.IO._
import java.io.PrintWriter
import java.io.BufferedOutputStream

class LolaExporter extends Exporter {
  val targetFormat = Format.LolaPetriNet

  def export(model: ModelServiceProvider): Either[ServiceNotAvailableException, ExportJob] = model.implementation(PetriNetSnapshotService) match {
    case Some(impl) => Right(new LolaExportJob(impl))
    case None => Left(new ServiceNotAvailableException(PetriNetSnapshotService))
  }
}

class LolaExportJob(net: PetriNetSnapshot) extends ExportJob {
  val complete = false
  def job(stream: OutputStream) = ioPure.pure {
    val writer = new PrintWriter(new BufferedOutputStream(stream))
    try {
      writer.println("PIS'KA!!!!!")
    } finally {
      writer.close()
    }
  }
}