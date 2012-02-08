package org.workcraft.tasks
import scalaz.effects.IO
import org.workcraft.scala.Expressions.ThreadSafeVariable
import org.workcraft.dependencymanager.advanced.core.GlobalCache
import org.workcraft.dependencymanager.advanced.core.Expression
import scalaz._
import Scalaz._



sealed abstract class Progress[+O, +E]

case object Suspended extends Progress[Nothing, Nothing]
case class KnownProgress(val progress: Double) extends Progress[Nothing, Nothing]
case object UnknownProgress extends Progress[Nothing, Nothing]
case class Completed[O, E](val result: Either[E, O]) extends Progress[O, E]

sealed trait Outcome[+O, +E]

case class Failed[E](val error: E) extends Outcome[Nothing, E]
case class Cancelled extends Outcome[Nothing, Nothing]
case class Finished[O](val output: O) extends Outcome[O, Nothing]

abstract class Task[O, E] {
  val progress = new ThreadSafeVariable[Progress[O, E]](Suspended)

  def runTask: Either[E, O]

  def map[O2](f: O => O2) = {
    val outer = this
    new Task[O2, E] {
      def runTask = {
        val outcome = outer.runTask
        outcome match {
          case Left(error) => Left(error)
          case Right(output) => Right(f(output))
        }
      }
    }
  }

  def flatMap[O2](f: O => Task[O2, E]) = {
    val outer = this
    new Task[O2, E] {
      def runTask = {
        val outcome = outer.runTask
        outcome match {
          case Left(error) => Left(error)
          case Right(output) => f(output).runTask
        }
      }
    }
  }

  def hFlatMap[O2, E2](f: O => Task[O2, E2]) = {
    val outer = this

    new Task[O2, Either[E, E2]] {
      def runTask = {
        val outcome = outer.runTask
        outcome match {
          case Left(error) => Left(Left(error))
          case Right(output) => f(output).runTask match {
            case Left(error) => Left(Right(error))
            case Right(output) => Right(output)
          }
        }
      }
    }
  }
}
 
object Task {
  def pure[O, E](outcome: O) = new Task[O, E] {
    def runTask = Right(outcome)
  }

  implicit def taskMonad[E] = new Monad[({ type λ[α] = Task[α, E] })#λ] {
    def bind[O1, O2](a: Task[O1, E], f: O1 => Task[O2, E]) = a.flatMap(f)
    def pure[O](a: => O) = Task.pure(a)
  }
  
  implicit def TaskMA[O, E](t: Task[O, E]): MA[({ type λ[α] = Task[α, E] })#λ, O] = ma[({ type λ[α] = Task[α, E] })#λ, O](t)
  
  implicit def taskMonadD = taskMonad[Double]
}