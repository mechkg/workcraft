package org.workcraft.services

abstract class Format(val description: String, val extension: String)

object Format {
  case object DotG extends Format("Signal Transition Graph", ".g")
  case object LolaPetriNet extends Format("Low level Petri Net in LoLA format", ".lola")
}