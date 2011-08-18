package org.workcraft.plugins.cpog.scala

import scalaz.Scalaz._
import scalaz.Monad
import org.workcraft.plugins.cpog.optimisation.BooleanFormula
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.VariableReplacer
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.BooleanReplacer
import org.workcraft.plugins.cpog.optimisation.expressions.BooleanOperations
import org.workcraft.plugins.cpog.optimisation.expressions.Variable
import org.workcraft.scala.Util._
import scalaz.Traverse
import scalaz.Applicative
import org.workcraft.plugins.cpog.optimisation.expressions.DumbBooleanWorker
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.CachedVisitor
import org.workcraft.plugins.cpog.optimisation.expressions.interfaces.FoldVisitor

object BooleanFormulaInstances {
  implicit object BFMonad extends Monad[BooleanFormula] with Traverse[BooleanFormula] {
    override def pure[T](v : => T) : BooleanFormula[T] = Variable.create(v)
    override def bind[A, B](a: BooleanFormula[A], f: A => BooleanFormula[B]): BooleanFormula[B] = {
      BooleanReplacer.cachedReplacer[A,B](BooleanOperations.worker, f)(a)
    }

    override def traverse[F[_] : Applicative, A, B](f: A => F[B], t: BooleanFormula[A]): F[BooleanFormula[B]] = {
	  type R=F[BooleanFormula[B]]
	  val worker = new DumbBooleanWorker
	  CachedVisitor.visitEachNodeOnce[A,R](new FoldVisitor[A, R]{
	    import worker._
	    override def visitOr(x:R, y:R)=(ma(x) <**> y) { or(_, _) }
	    override def visitAnd(x:R, y:R)=(ma(x) <**> y) { and(_, _) }
	    override def visitXor(x:R, y:R)=(ma(x) <**> y) { xor(_, _) }
	    override def visitImply(x:R, y:R)=(ma(x) <**> y) { imply(_, _) }
	    override def visitIff(x:R, y:R)=(ma(x) <**> y) { iff(_, _) }
	    override def visitNot(x:R)=ma(x) map { not(_) }
	    override def visitVariable(v:A)=f(v) map (`var`(_) )
	    override def visitOne()=scalaz.Scalaz.pure[F].apply(one[B])
	    override def visitZero()=scalaz.Scalaz.pure[F].apply(zero[B])
	  }, t);
	}
  }
}
