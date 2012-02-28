package org.workcraft.plugins.petri.tools

object TracePair {
  import scalaz.Lens
  def createEmpty = TracePair(TraceStep(Trace(Nil),0),TraceStep(Trace(Nil),0))
  val main : Lens[TracePair, TraceStep] = Lens(p => p.main, (p, m) => p.copy(main = m))
  val branch : Lens[TracePair, TraceStep] = Lens(p => p.branch, (p, m) => p.copy(branch = m))
}

case class TracePair(val main : TraceStep, val branch : TraceStep)

