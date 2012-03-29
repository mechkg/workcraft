package org.workcraft.plugins.petrify
import org.workcraft.services.ExportError

sealed trait PetrifyError 

object PetrifyError {
  case class CouldNotStart (cause: Throwable) extends PetrifyError
  case class DotGExportError (cause: ExportError) extends PetrifyError
  case class RuntimeError (output: String) extends PetrifyError
  case class DotGParseError (cause: String) extends PetrifyError
} 