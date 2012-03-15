package org.workcraft.plugins.petri2
import org.workcraft.services.FileOpen
import java.io.File
import org.workcraft.services.FileOpenJob
import org.workcraft.scala.effects.IO
import org.workcraft.scala.effects.IO._
import org.workcraft.services.Format

object PnFileOpen extends FileOpen {
  lazy val parser = new PnFormatParser
  
  val description = "Default Workcraft Petri Net format importer"
  val sourceFormat = Format.WorkcraftPetriNet
  
  def open(file: File) = ioPure.pure {
    if (file.getName().endsWith(".pn")) Some(FileOpenJob(ioPure.pure {
      parser.parse(file) match {
        case Left(error) => Left(error)
        case Right(VisualPetriNet(pn, layout)) => Right(new PetriNetModel(pn, layout))
      }
    }))
    else None
  }
}