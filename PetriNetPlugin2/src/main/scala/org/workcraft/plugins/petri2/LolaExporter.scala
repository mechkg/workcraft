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

object LolaExporter extends Exporter {
  val targetFormat = Format.LolaPetriNet

  def export(model: ModelServiceProvider): Either[ServiceNotAvailableException, ExportJob] = model.implementation(PetriNetSnapshotService) match {
    case Some(impl) => Right(new LolaExportJob(impl))
    case None => Left(new ServiceNotAvailableException(PetriNetSnapshotService))
  }
}

class LolaExportJob(snapshot: IO[PetriNetSnapshot]) extends ExportJob {
  val complete = false
  
  def context (net: PetriNetSnapshot) : (Map[Transition, List[Place]], Map[Transition, List[Place]]) =
    net.arcs.foldRight((Map[Transition, List[Place]]().withDefault(_ => List()), Map[Transition, List[Place]]().withDefault(_ => List())))( {case (arc, (prod, cons)) => arc match {
      case c: ConsumerArc => (prod, cons + ( c.to -> (c.from :: cons(c.to)) ))
      case p: ProducerArc => (prod + (p.from -> (p.to :: prod(p.from))), cons)
    }})
  
  def job(stream: OutputStream) = snapshot >>= ( net => ioPure.pure {
    val writer = new PrintWriter(new BufferedOutputStream(stream))
    try {
      val (prod, cons) = context(net)
      
      val places = "PLACE " + net.places.map( p => net.labelling(p)).mkString(", ") + ";"
      val marking = "MARKING " + net.places.map ( p => net.labelling(p) + ": " + net.marking(p) ).mkString(", ") + ";"
      val transitions = net.transitions.map (t => 
        "TRANSITION " + net.labelling(t) + 
        " CONSUME " + cons(t).map(p => net.labelling(p) + ": 1").mkString(", ") + "; " +
        " PRODUCE " + prod(t).map(p => net.labelling(p) + ": 1").mkString(", ") + ";").mkString ("\n")
        
        
      writer.println ( places + "\n" + marking + "\n" + transitions)
    } finally {
      writer.close()
    }
  })
}