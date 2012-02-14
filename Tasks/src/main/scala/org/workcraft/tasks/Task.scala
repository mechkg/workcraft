package org.workcraft.tasks
import org.workcraft.scala.Expressions.ThreadSafeVariable
import org.workcraft.dependencymanager.advanced.core.GlobalCache
import org.workcraft.dependencymanager.advanced.core.Expression
import scalaz._
import Scalaz._
import org.workcraft.scala.effects.IO
import org.workcraft.scala.effects.IO._

trait TaskControl {
  val cancelRequest : IO[Boolean]
  val progressUpdate: Double => IO[Unit]
}

trait Task[+O, +E] {
  import Task._
  
  def runTask (tc : TaskControl) : IO[Either[Option[E], O]]

  def flatMap[O2, E2 >: E](f: O => Task[O2, E2]) = {
    val outer = this
    
    new Task[O2, E2] {
      def runTask (tc: TaskControl) = outer.runTask(tc) >>= {
        case Left(error) => Left(error).pure[IO]
        case Right(output) => tc.cancelRequest.>>=[Either[Option[E2], O2]] {
          case true => Left(None).pure[IO]
          case false => f(output).runTask(tc)
        }
      }
    }
  }

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

  def runAsynchronously (tc: TaskControl) : IO[Unit] = {
    // run task in a separate thread
    val thread = new Thread() {
      override def run() = runTask(tc).unsafePerformIO 
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
  
  def forward[E, O] (result: Either[Option[E],O], action: IO[Unit]) = new Task[O,E] {
    def runTask(tc: TaskControl) = action >>= (x => result.pure[IO])
  }
}