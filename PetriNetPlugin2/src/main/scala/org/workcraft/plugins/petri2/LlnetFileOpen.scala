package org.workcraft.plugins.petri2
import java.io.File
import org.workcraft.services.FileOpen
import org.workcraft.services.FileOpenJob
import org.workcraft.services.Format
import org.workcraft.scala.effects.IO._
import scalaz.Scalaz._

object LlnetFileOpen extends FileOpen {
  val description = "Workcraft .ll_net format importer"
  val sourceFormat = Format.Llnet

  def checkFile(file: File) = ioPure.pure { file.getName().endsWith(".ll_net") }

  override def open(file: File) = checkFile(file).map(
    if (_)
      Some(FileOpenJob(
      LlnetParser.parseLlnet(file) >>= {
        case Left(error) => ioPure.pure { Left(error) }
        case Right(llnet) => PetriNetBuilder.buildPetriNet(llnet) >>= 
	  (pn => EditablePetriNet.create(pn).map( epn => Right( new PetriNetModel (epn))))
      }))
    else
      None)
}
