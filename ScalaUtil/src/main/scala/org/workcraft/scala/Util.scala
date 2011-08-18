package org.workcraft.scala

import scalaz.Monad
import java.util.HashMap
import org.workcraft.dependencymanager.advanced.core.ExpressionBase
import org.workcraft.dependencymanager.advanced.core.EvaluationContext
import org.workcraft.dependencymanager.advanced.core.GlobalCache
import org.workcraft.dependencymanager.advanced.user.ModifiableExpressionBase
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression
import org.workcraft.util.FieldAccessor
import org.workcraft.util.Maybe
import org.workcraft.dependencymanager.advanced.core.Expressions.{ fmap => javafmap, bind => javabind, asFunction, constant }
import org.workcraft.dependencymanager.advanced.core.Expression
import org.workcraft.util.Function
import org.workcraft.util.Function0
import org.workcraft.util.Function2
import org.workcraft.dependencymanager.advanced.user.Setter
import org.workcraft.util.MaybeVisitor
import org.workcraft.util.Action1

object Util {
  
  implicit def asFunctionObject[T, R](f: (T => R)) = new Function[T, R] {
    def apply(x: T) = f(x)
  }
  
  implicit def asFunctionObject0[R](f: () => R) = new Function0[R] {
    def apply() = f()
  }

  implicit def asFunctionObject2[T1, T2, R](f: ((T1, T2) => R)) = new Function2[T1, T2, R] {
    def apply(x: T1, y: T2) = f(x, y)
  }
  
  implicit def setterAsAction[T](setter : Setter[T]) : Action1[T] = new Action1[T] {
    override def run(t : T) {
      setter.setValue(t)
    }
  }
  
  implicit def unitFunctionAsAction1[T](f : T => Unit) : Action1[T] = new Action1[T]{
    override def run(t : T) = f(t)
  }
  
  implicit def asOption[T] (mb : Maybe[T]) : Option[T] = 
    mb.accept ( new MaybeVisitor[T, Option[T]] {
      def visitNothing = None
      def visitJust (t : T) = Some(t)
    })
    
  implicit def asMaybe[T] (o : Option[T]) : Maybe[T] = o match {
    case None => Maybe.Util.nothing[T]
    case Some(x) => Maybe.Util.just(x)
  }

  def bindFunc[A, B](a: Expression[_ <: A])(f: A => B): Expression[B] = javafmap(asFunctionObject(f), a)
  def bindFunc[A, B, C](a: Expression[_ <: A], b: Expression[_ <: B])(f: (A, B) => C): Expression[C] = javafmap(asFunctionObject2(f), a, b)
  def fmap[A, B](f: A => B)(a: Expression[_ <: A]): Expression[B] = javafmap(asFunctionObject(f), a)
  def fmap2[A, B, R](f: (A, B) => R)(a: Expression[_ <: A], b: Expression[_ <: B]): Expression[R] = javafmap(asFunctionObject2(f), a, b)

  def fmap[A, B, C](f: (A, B) => C)(a: Expression[_ <: A], b: Expression[_ <: B]): Expression[C] = javafmap(asFunctionObject2(f), a, b)
  def bind[A, B](a: Expression[_ <: A], f: A => _ <: Expression[_ <: B]): Expression[B] = javabind[A, B](a, asFunctionObject(f))

  def applyFieldAccessor[T, FieldT](expr: ModifiableExpression[T], accessor: FieldAccessor[T, FieldT]): ModifiableExpression[FieldT] =
    new ModifiableExpressionBase[FieldT] {
      override def setValue(newValue: FieldT) = expr.setValue(accessor.assign(GlobalCache.eval(expr), newValue));
      override def evaluate(context: EvaluationContext): FieldT = accessor(context.resolve(expr))
    }

  def javaCollectionToList[A](c : java.lang.Iterable[_ <: A]) = {
    var res : List[A] = Nil
    val it = c.iterator
    while(it.hasNext)
    	res = it.next :: res
    res.reverse
  }
}
