package org.workcraft.tasks
import org.workcraft.scala.Expressions.ThreadSafeVariable
import org.workcraft.dependencymanager.advanced.core.GlobalCache
import org.workcraft.dependencymanager.advanced.core.Expression
import scalaz._
import Scalaz._
import org.workcraft.scala.effects.IO
import org.workcraft.scala.effects.IO._

sealed abstract class Progress[+O, +E]

case object Suspended extends Progress[Nothing, Nothing]
case class KnownProgress(val progress: Double) extends Progress[Nothing, Nothing]
case object UnknownProgress extends Progress[Nothing, Nothing]
case class Completed[O, E](val result: Either[E, O]) extends Progress[O, E]

sealed trait Outcome[+O, +E]

case class Failed[E](val error: E) extends Outcome[Nothing, E]
case class Cancelled extends Outcome[Nothing, Nothing]
case class Finished[O](val output: O) extends Outcome[O, Nothing]

trait Task[+O, +E] {
  import Task._
  
  def runTask (cancelRequest : IO[Boolean]) : IO[Either[Option[E], O]]

  def flatMap[O2, E2 >: E](f: O => Task[O2, E2]) = {
    val outer = this
    
    new Task[O2, E2] {
      def runTask (cancelRequest: IO[Boolean]) = outer.runTask(cancelRequest) >>= {
        case Left(error) => Left(error).pure[IO]
        case Right(output) => cancelRequest.>>=[Either[Option[E2], O2]] {
          case true => Left(None).pure[IO]
          case false => f(output).runTask(cancelRequest)
        }
      }
    }
  }

  def mapError[E2 >: E](f: E => E2) = {
    val outer = this
    new Task[O, E2] {
      def runTask (cancelRequest: IO[Boolean]) = outer.runTask(cancelRequest) map {
        case Left(None) => Left(None)
        case Left(Some(error)) => Left(Some(f(error)))
        case Right(output) => Right(output)
      }
    }
  }

  def runAsynchronously (cancelRequest : IO[Boolean]) : IO[Unit] = {
    // run task in a separate thread
    val thread = new Thread() {
      override def run() = runTask(cancelRequest).unsafePerformIO 
    }

    thread.start()

 /*    // monitor cancel request and task completion    
    while (true) {
      if (cancelRequest)
        return runCancel.unsafePerformIO
      else if (!thread.isAlive) thread.result match {
        case Some(result) => return result
        case None => throw new RuntimeException("Task thread finished but the result was None")
      }
      else
        Thread.sleep(30)
    }

    throw new RuntimeException("to shut up the compiler") */
  }.pure[IO]
}

object Task {
  def pure[O, E](outcome: O) = new Task[O, E] {
    def runTask(cancelRequest: IO[Boolean]) = Right(outcome).pure[IO]
  }

  implicit def taskMonad[E] = new Monad[({ type λ[α] = Task[α, E] })#λ] {
    def bind[O1, O2](a: Task[O1, E], f: O1 => Task[O2, E]) = a.flatMap(f)
    def pure[O](a: => O) = Task.pure(a)
  }

  implicit def taskMA[O, E](t: Task[O, E]): MA[({ type λ[α] = Task[α, E] })#λ, O] = ma[({ type λ[α] = Task[α, E] })#λ, O](t)
  
  def apply[E,O] (task: IO[Either[E, O]]) = new Task[O,E] {
    def runTask(cancelRequested: IO[Boolean]) = task map {
      case Left(error) => Left(Some(error))
      case Right(value) => Right(value)
    }
  }
  
  def apply[E,O] (task: IO[Boolean] => IO[Either[Option[E],O]]) = new Task[O,E] {
    def runTask(cancelRequested: IO[Boolean]) = task(cancelRequested)
  }
}