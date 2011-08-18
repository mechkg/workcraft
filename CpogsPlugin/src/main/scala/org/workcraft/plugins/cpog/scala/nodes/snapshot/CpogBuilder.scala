package org.workcraft.plugins.cpog.scala.nodes.snapshot

import scalaz.Bind
import scalaz.Pure
import scalaz.Monad
import org.workcraft.scala.Expressions.Expression
import org.workcraft.plugins.cpog.scala.{nodes=>M}

trait CpogBuilder[+T] {
  self =>
  def buildWithCpog(cpog: CpogBuilding): (CpogBuilding, T)
  def map[R](f: T => R) = new CpogBuilder[R] {
    def buildWithCpog(cpog: CpogBuilding): (CpogBuilding, R) = {
      self.buildWithCpog(cpog) match {
        case (cpog, v) => (cpog, f(v))
      }
    }
  }
  def flatMap[R](f: T => CpogBuilder[R]) = new CpogBuilder[R] {
    def buildWithCpog(cpog: CpogBuilding): (CpogBuilding, R) = {
      self.buildWithCpog(cpog) match {
        case (cpog, v) => f(v).buildWithCpog(cpog)
      }
    }
  }
}

object CpogBuilder {
  implicit object MonadInstance extends Monad[CpogBuilder] {
    override def bind[A,B](a : CpogBuilder[A], b : A => CpogBuilder[B]) = a.flatMap(b)
    override def pure[A](a: => A): CpogBuilder[A] = emptyBuilder[A](a)
  }
  
  def emptyBuilder[T](result : T) : CpogBuilder[T] = new CpogBuilder[T] {
    override def buildWithCpog(cpog : CpogBuilding) = (cpog, result)
  }
}

case class CpogBuilding(cpog : Expression[CPOG], varCache : Map[M.Variable, Id[Variable]], vertexCache : Map[M.Vertex, Id[Vertex]]) {
}
