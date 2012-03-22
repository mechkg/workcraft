package org.workcraft.swing

import scalaz.Monad

import org.workcraft.scala.effects.IO
import org.workcraft.scala.effects.IO._

class Swing[A] (run : IO[A]) {
  // assumes that it will be executed on the Swing thread.
  def unsafeRun : IO[A] = run
  def unsafePerformIO = unsafeRun.unsafePerformIO
}

object Swing {
  
  class SwingRef[A] private[swing] (iv : A) {
    var cv = iv
    def read : Swing[A] = liftIO(ioPure.pure(cv))
    def write(v : A) : Swing[Unit] = liftIO(ioPure.pure(cv = v))
    def mod(f: A => A) : Swing[Unit] = liftIO(ioPure.pure{cv = f(cv)})
  }

  def newRef[A] (a : A) : Swing[SwingRef[A]] = liftIO(IO.ioPure.pure(new SwingRef(a)))
  

  def liftIO[A](run : IO[A]) = new Swing(run)
  def unsafeToSwing[A](run : => A) = liftIO(IO.ioPure.pure(run))
  
  implicit def swingMonad : Monad[Swing] = new Monad[Swing] {
    override def bind[A,B](x : Swing[A], f : A => Swing[B]) : Swing[B] = liftIO(x.unsafeRun flatMap (a => f(a).unsafeRun))
    override def pure[A](x : => A) : Swing[A] = liftIO(IO.ioPure.pure(x))
  }
}
