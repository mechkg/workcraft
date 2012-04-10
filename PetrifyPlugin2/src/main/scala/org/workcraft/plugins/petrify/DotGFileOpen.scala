package org.workcraft.plugins.petrify
import java.io.File
import org.workcraft.plugins.petri2.PnFormatParser
import org.workcraft.services.FileOpen
import org.workcraft.services.FileOpenJob
import org.workcraft.services.Format
import org.workcraft.scala.effects.IO._

object DotGFileOpen extends FileOpen {
  val description = "Workcraft .g format importer"
  val sourceFormat = Format.DotG

  def checkFile(file: File) = ioPure.pure { file.getName().endsWith(".g") }

  override def open(file: File) = checkFile(file).map(
    if (_)
      Some(FileOpenJob(
	DotGParser.parseDotG (file).map {
	  case Left(error) => Left(error)
	  case Right(dotg) => PetriNetBuilder.buildPetriNet(dotg).map(pn => Right(new PetriNetModel(n)))}.join))
    else 
      None)
}
