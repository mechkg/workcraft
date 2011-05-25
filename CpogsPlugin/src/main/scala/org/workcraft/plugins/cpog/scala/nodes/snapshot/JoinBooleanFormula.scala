package org.workcraft.plugins.cpog.scala.nodes.snapshot

import scalaz.MA
import scalaz.Monad
import org.workcraft.plugins.cpog.optimisation.expressions.DumbBooleanWorker
import org.workcraft.plugins.cpog.optimisation.booleanvisitors.CachedVisitor
import org.workcraft.plugins.cpog.optimisation.expressions.Or
import org.workcraft.plugins.cpog.optimisation.expressions.interfaces.FoldVisitor
import org.workcraft.plugins.cpog.optimisation.BooleanFormula
import org.workcraft.plugins.cpog.scala.Scalaz._

object JoinBooleanFormula {
  
	def joinBooleanFormula[M[_], V](f : BooleanFormula[M[V]])(implicit monad : Monad[M]) : M[BooleanFormula[V]] = {
	  type R=M[BooleanFormula[V]]
	  val worker = new DumbBooleanWorker
	  CachedVisitor.visitEachNodeOnce(new FoldVisitor[M[V], M[BooleanFormula[V]]]{
	    import worker._
	    override def visitOr(x:R, y:R)=for(x<-ma(x);y<-ma(y))yield or(x,y)
	    override def visitAnd(x:R, y:R)=for(x<-ma(x);y<-ma(y))yield and(x,y)
	    override def visitXor(x:R, y:R)=for(x<-ma(x);y<-ma(y))yield xor(x,y)
	    override def visitImply(x:R, y:R)=for(x<-ma(x);y<-ma(y))yield imply(x,y)
	    override def visitIff(x:R, y:R)=for(x<-ma(x);y<-ma(y))yield iff(x,y)
	    override def visitNot(x:R)=for(x<-ma(x))yield not(x)
	    override def visitVariable(v:M[V])=for(v<-v)yield `var`(v)
	    override def visitOne()= monad.pure(one[V])
	    override def visitZero()=monad.pure(zero[V])
	  }, f);
	}
}
