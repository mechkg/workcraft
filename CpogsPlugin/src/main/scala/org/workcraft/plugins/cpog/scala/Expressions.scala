package org.workcraft.plugins.cpog.scala

import org.workcraft.dependencymanager.advanced.user.ModifiableExpression
import scalaz.Monad
import scala.collection.generic.CanBuildFrom
import scala.collection.TraversableLike
import org.workcraft.dependencymanager.advanced.core.Expression
import org.workcraft.dependencymanager.advanced.core.Expressions.{joinCollection => joinCollectionJ, constant => constantJ, bind => bindJ, fmap => fmapJ}
import scala.collection.JavaConversions.{asJavaCollection, asScalaIterable}
import Util._
import Expressions._
import Scalaz._

object Expressions {
  
  implicit object ExpressionMonad extends Monad[Expression] {
    override def pure[A] (x : => A) = Expressions.constant(x)
    override def bind[A,B](a : Expression[A], f : A => Expression[B]) : Expression[B] = bindJ(a, asFunctionObject(f))
  }
  
  /**
   *  Needed because Scala is stupid!
   */
  implicit def monadicSyntax[A](m: ModifiableExpression[A]) = new {
    def map[B](f: A => B) = implicitly[Monad[Expression]].fmap(m, f)
    def flatMap[B](f: A => Expression[B]) = implicitly[Monad[Expression]].bind(m, f)
  }
  
  def constant[A](a : A) = constantJ(a)
  
  trait ExpressionOps[+A] {
    def mapE[B](f : A => Expression[_ <: B]) : Expression[List[B]]
  }
  
  implicit def augmentWithExpressionOps[A, Coll <: Iterable[A]](coll : Coll) : ExpressionOps[A] = {
    new ExpressionOps[A]() {
      def mapE[B](f : A => Expression[_ <: B]) = {
        joinCollection[B](coll.map(f))
      }
    }
  }
  
//  implicit def asExpresion[A](e : ModifiableExpression[A]) : Expression[A] = e
  
  def joinCollection[A](collection : Iterable[Expression[_ <: A]]) : Expression[List[A]] = collection.foldRight(constant(Nil : List[A]))((head : Expression[_ <: A], tail : Expression[List[A]]) => for(tail <- tail; head <- head) yield head::tail)
}
