package org.workcraft.tasks

sealed trait Progress[T]

object Progress {
  case class KnownProgress[T] (val progress: Double) extends Progress[T]
  case class UnknownProgress[T] extends Progress[T]
  case class Completed[T] (val result: T) extends Progress[T]
}

trait Task[T] {
  def run : IO[Expression[Progress]] 
}