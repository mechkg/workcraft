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
import scalaz._
import Scalaz._
import java.io.FileOutputStream
import java.io.File
import org.workcraft.services.ExportError

object PnExporter extends Exporter {
  val targetFormat = Format.WorkcraftPetriNet

  def export(model: ModelServiceProvider): Either[ServiceNotAvailableException, ExportJob] = model.implementation(VisualPetriNetService) match {
    case Some(impl) => Right(new PnExportJob(impl))
    case None => Left(new ServiceNotAvailableException(VisualPetriNetService))
  }
}

class PnExportJob(snapshot: IO[VisualPetriNet]) extends ExportJob {
  def job(file: File) = snapshot >>= (net => ioPure.pure {
    var writer = new PrintWriter(new BufferedOutputStream(new FileOutputStream(file)))
    try {
      val VisualPetriNet(pn, layout, visualArcs) = snapshot.unsafePerformIO

      writer.println("Places:")
      writer.println(pn.places.map(p => pn.labelling(p)).mkString(" "))
      writer.println("Transitions:")
      writer.println(pn.transitions.map(t => pn.labelling(t)).mkString(" "))
      writer.println("Arcs:")
      writer.println(pn.arcs.map(a => "(" + pn.labelling(a.from) + " " + pn.labelling(a.to) + ")").mkString(" "))
      writer.println("Marking:")
      writer.println(pn.places.map(p => "(" + pn.labelling(p) + " " + pn.marking(p) + ")").mkString(" "))
      writer.println("Layout:")
      writer.println(layout.toList.map({ case (component, position) => "(" + pn.labelling(component) + " " + position.getX + " " + position.getY + ")" }).mkString(" "))

      None
    } catch {
      case e => Some(ExportError.Exception(e))
    } finally {
      writer.close()
    }
  })
}