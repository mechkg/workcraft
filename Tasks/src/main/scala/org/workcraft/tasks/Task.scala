package org.workcraft.tasks
import scalaz.effects.IO

import org.workcraft.scala.Expressions._

sealed trait Progress[O, E]

case class KnownProgress (val progress: Double) extends Progress[Any, Any]
case class UnknownProgress extends Progress[Any, Any]
case class Completed[O, E] (val result: Outcome[O, E]) extends Progress[O, E]

sealed trait Outcome[O, E]

case class Failed[E](val error: E) extends Outcome[Any,E]
case class Cancelled extends Outcome[Any, Any]
case class Finished[O](val output: O) extends Outcome[O, Any]
 

trait Task[I, O, E] {
  def description : Expression[String]
  def action (input: I) : IO[Expression[Progress[O, E]]]
 
  def eval[T](e:Expression[T]) : T
  
  def start (input: I): Outcome[O, E] = {
    
  }
  
  def run (input: I) : Outcome [O, E] = {
      val p = action(input).unsafePerformIO
      
      while (true) {
        eval(p) match {
          case Completed(outcome) => return outcome
          case _ => Thread.sleep(30)
       }
      }
      
      throw new RuntimeException("stfu")
  }
}