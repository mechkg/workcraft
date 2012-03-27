package org.workcraft.tasks
import org.workcraft.scala.Expressions.ThreadSafeVariable
import org.workcraft.dependencymanager.advanced.core.GlobalCache
import org.workcraft.dependencymanager.advanced.core.Expression
import scalaz._
import Scalaz._
import org.workcraft.scala.effects.IO
import org.workcraft.scala.effects.IO._

case class TaskControl(cancelRequest : IO[Boolean], progressUpdate: Double => IO[Unit], descriptionUpdate: String => IO[Unit])

trait Task[+O, +E] {
  import Task._
  
  def runTask (tc : TaskControl) : IO[Either[Option[E], O]]

  def flatMap[O2, E2 >: E](f: O => Task[O2, E2]) = {
    val outer = this
    
    new Task[O2, E2] {
      def runTask (tc: TaskControl) = outer.runTask(tc) >>= {
        case Left(error) => ioPure.pure { Left(error) }
        case Right(output) => tc.cancelRequest.>>=[Either[Option[E2], O2]] {
          case true => ioPure.pure { Left(None) }
          case false => f(output).runTask(tc)
        }
      }
    }
  }
  
  def >>= [O2, E2 >: E](f: O => Task[O2, E2]) = flatMap(f)
  
  def >>=| [O2, E2 >: E] (t: Task[O2, E2]) = flatMap(_ => t)

  def mapError[E2 >: E](f: E => E2) = {
    val outer = this
    new Task[O, E2] {
      def runTask (tc: TaskControl) = outer.runTask(tc) map {
        case Left(None) => Left(None)
        case Left(Some(error)) => Left(Some(f(error)))
        case Right(output) => Right(output)
      }
    }
  }
  
   def mapError2[E2] (f: E => E2) = {
    val outer = this
    new Task[O, E2] {
      def runTask (tc: TaskControl) = outer.runTask(tc) map {
        case Left(None) => Left(None)
        case Left(Some(error)) => Left(Some(f(error)))
        case Right(output) => Right(output)
      }
    }
  }

  def runAsynchronously (tc: TaskControl, finished: Either[Option[E], O] => IO[Unit]) : IO[Unit] = ioPure.pure {
    new Thread() {
      override def run() = (runTask(tc) >>= finished).unsafePerformIO
    }.start()
  }
}

object Task {
  def pure[O, E](outcome: O) = new Task[O, E] {
    def runTask(tc: TaskControl) = Right(outcome).pure[IO]
  }

  implicit def taskMonad[E] = new Monad[({ type λ[α] = Task[α, E] })#λ] {
    def bind[O1, O2](a: Task[O1, E], f: O1 => Task[O2, E]) = a.flatMap(f)
    def pure[O](a: => O) = Task.pure(a)
  }

  implicit def taskMA[O, E](t: Task[O, E]): MA[({ type λ[α] = Task[α, E] })#λ, O] = ma[({ type λ[α] = Task[α, E] })#λ, O](t)
  
  def apply[E,O] (task: IO[Either[E, O]]) = new Task[O,E] {
    def runTask(tc: TaskControl) = task map {
      case Left(error) => Left(Some(error))
      case Right(value) => Right(value)
    }
  }
  
  def apply[E,O] (task: TaskControl => IO[Either[Option[E],O]]) = new Task[O,E] {
    def runTask(tc: TaskControl) = task(tc)
  }
  
  implicit def ioTask[E] (action: IO[Unit]) = new Task[Unit, E] {
    def runTask(tc: TaskControl) = action >>= (x => Right(()).pure[IO])
  }
}