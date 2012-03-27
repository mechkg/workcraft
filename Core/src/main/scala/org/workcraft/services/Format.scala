package org.workcraft.services

abstract class Format(val description: String, val extension: String)

object Format {
  case object DotG extends Format("Signal Transition Graph", ".g")
  case object LolaPetriNet extends Format("Petri Net in LoLA format", ".lola")
  case object WorkcraftPetriNet extends Format("Petri Net in Workcraft format", ".pn")
  case object Dot extends Format("GraphViz dot", ".dot")
}