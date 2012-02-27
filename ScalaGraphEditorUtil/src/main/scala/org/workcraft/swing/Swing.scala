package org.workcraft.swing

import scalaz.Monad
import scalaz.effects.IO
import scalaz.effects.IORef
import scalaz.effects.STRef

class Swing[A] (run : IO[A]) {
  // assumes that it will be executed on the Swing thread.
  def unsafeRun : IO[A] = run
  def unsafePerformIO = unsafeRun.unsafePerformIO
}

object Swing {
  
  class SwingRef[A](ref : IORef[A]) {
    def read = liftIO(ref.read)
    def write(v : A) = liftIO(ref.write(v))
    def mod(f: A => A) = liftIO(ref.mod(f))
  }

  def newRef[A] (a : A) : Swing[SwingRef[A]] = liftIO(IO.ioPure.pure(new SwingRef(new IORef[A](new STRef(a)))))
  

  def liftIO[A](run : IO[A]) = new Swing(run)
  def unsafeToSwing[A](run : => A) = liftIO(IO.ioPure.pure(run))
  
  implicit def swingMonad : Monad[Swing] = new Monad[Swing] {
    override def bind[A,B](x : Swing[A], f : A => Swing[B]) : Swing[B] = liftIO(x.unsafeRun flatMap (a => f(a).unsafeRun))
    override def pure[A](x : => A) : Swing[A] = liftIO(IO.ioPure.pure(x))
  }
}
