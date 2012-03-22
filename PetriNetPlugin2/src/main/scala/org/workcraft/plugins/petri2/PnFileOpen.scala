package org.workcraft.plugins.petri2
import org.workcraft.services.FileOpen
import java.io.File
import org.workcraft.services.FileOpenJob
import org.workcraft.scala.effects.IO
import org.workcraft.scala.effects.IO._
import org.workcraft.services.Format
import org.workcraft.services.ModelServiceProvider
import scalaz.Scalaz._

object PnFileOpen extends FileOpen {
  lazy val parser = new PnFormatParser

  val description = "Default Workcraft Petri Net format importer"
  val sourceFormat = Format.WorkcraftPetriNet

  def checkFile(file: File) = ioPure.pure { file.getName().endsWith(".pn") }

  override def open(file: File) = checkFile(file).map(
    if (_)
      Some(FileOpenJob(parser.parse(file).map {case Left(error) => ioPure.pure {Left(error)}; case Right(vpn) => EditablePetriNet.create(vpn).map(n => Right(new PetriNetModel(n)))}.join))
    else None)
}