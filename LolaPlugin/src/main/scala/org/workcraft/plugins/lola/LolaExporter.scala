package org.workcraft.plugins.lola

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
import org.workcraft.plugins.petri2.PetriNetService
import org.workcraft.plugins.petri2.PetriNet
import org.workcraft.plugins.petri2.Transition
import org.workcraft.plugins.petri2.Place
import org.workcraft.plugins.petri2.ConsumerArc
import org.workcraft.plugins.petri2.ProducerArc
import java.io.File
import java.io.FileOutputStream
import org.workcraft.services.ExportError

object LolaExporter extends Exporter {
  val targetFormat = Format.LolaPetriNet

  def export(model: ModelServiceProvider): Either[ServiceNotAvailableException, ExportJob] = model.implementation(PetriNetService) match {
    case Some(impl) => Right(new LolaExportJob(impl))
    case None => Left(new ServiceNotAvailableException(PetriNetService))
  }
}

class LolaExportJob(snapshot: IO[PetriNet]) extends ExportJob {
  val complete = false

  def job(file: File) = snapshot >>= (net => ioPure.pure {
    var writer: PrintWriter = null
    if (net.places.isEmpty) Some(ExportError.Message("LoLA does not support nets with no places."))
    else if (net.transitions.isEmpty) Some(ExportError.Message("LoLA does not support nets with no transitions."))
    else
      try {

        writer = new PrintWriter(new BufferedOutputStream(new FileOutputStream(file)))
        val (prod, cons) = net.incidence

        val places = "PLACE " + net.places.map(p => net.labelling(p)).mkString(", ") + ";"
        val marking = "MARKING " + net.places.map(p => net.labelling(p) + ": " + net.marking(p)).mkString(", ") + ";"
        val transitions = if (net.transitions.isEmpty) "" else net.transitions.map(t =>
          "TRANSITION " + net.labelling(t) +
            " CONSUME " + cons(t).map(p => net.labelling(p) + ": 1").mkString(", ") + "; " +
            " PRODUCE " + prod(t).map(p => net.labelling(p) + ": 1").mkString(", ") + ";").mkString("\n")

        writer.println(places + "\n" + marking + "\n" + transitions)

        None
      } catch {
        case e => Some(ExportError.Exception(e))
      } finally {
        if (writer != null)
          writer.close()
      }
  })
}