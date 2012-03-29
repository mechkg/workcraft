package org.workcraft.plugins.dot
import org.workcraft.services.Exporter
import org.workcraft.services.Format
import org.workcraft.services.ModelServiceProvider
import org.workcraft.services.ServiceNotAvailableException
import org.workcraft.services.ExportJob
import java.io.File
import java.io.PrintStream
import java.io.BufferedOutputStream
import java.io.FileOutputStream
import org.workcraft.scala.effects.IO
import org.workcraft.scala.effects.IO._
import org.workcraft.services.ExportError
import org.workcraft.services.LayoutService
import scalaz.Scalaz._
import org.workcraft.services.LayoutSpec

/*object DotExporter extends Exporter {
  val targetFormat = Format.Dot

  def export(model: ModelServiceProvider): Either[ServiceNotAvailableException, ExportJob] = model.implementation(LayoutService) match {
    case Some(impl) => Right(new DotExportJob(impl ))
    case None => Left(new ServiceNotAvailableException(LayoutService))
  }
}*/

class DotExportJob(layout: LayoutSpec) extends ExportJob {
  def job(file: File) = ioPure.pure {
    val out = new PrintStream(new BufferedOutputStream(new FileOutputStream(file)))
    try {
      out.println("digraph work {");
      out.println("graph [nodesep=\"" + layout.nodeSeparation + "\", ranksep=\"" + layout.rankSeparation + "\", overlap=\"false\", splines=\"true\"];");
      out.println("node [shape=box];");

      val nodeToId = layout.nodes.zipWithIndex.toMap
      val idToNode = nodeToId.map(_.swap).toMap

      layout.nodes.foreach(node => {
        val (width, height) = layout.size(node)
        
        out.println("\"" + nodeToId(node) + "\" [width=\"" + width + "\", height=\"" + height + "\", fixedsize=\"true\"];");

        layout.outgoingArcs(node).foreach({ node2 =>
          out.println("\"" + nodeToId(node) + "\" -> \"" + nodeToId(node2) + "\";");
        })
      })
      out.println("}")
      
      None
    } catch {
      case e => Some(ExportError.Exception(e))
    } finally {
      out.close
    }
  }
}		