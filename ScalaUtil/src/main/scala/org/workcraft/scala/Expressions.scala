package org.workcraft.scala

import org.workcraft.dependencymanager.advanced.user.{ ModifiableExpression => JModifiableExpression, Variable }
import scalaz._
import scala.collection.generic.CanBuildFrom
import scala.collection.TraversableLike
import org.workcraft.dependencymanager.advanced.core.{ Expression => JExpression }
import org.workcraft.dependencymanager.advanced.core.Expressions.{ constant => jConstant, modifiableExpression => jModifiableExpression, joinCollection => joinCollectionJ, bind => bindJ, fmap => fmapJ }
import org.workcraft.dependencymanager.advanced.core.GlobalCache
import scala.collection.JavaConversions.{ asJavaCollection, asScalaIterable }
import Expressions._
import org.workcraft.scala.Scalaz._
import org.workcraft.scala.Util._
import org.workcraft.dependencymanager.advanced.user.Setter
import javax.swing.SwingUtilities
import org.workcraft.scala.effects.IO
import org.workcraft.scala.effects.IO._
import org.workcraft.dependencymanager.util.listeners.Listener
import org.workcraft.dependencymanager.advanced.core.ExpressionBase.ValueHandleTuple
import org.workcraft.dependencymanager.advanced.user.AutoRefreshExpression
import org.workcraft.dependencymanager.advanced.core.EvaluationContext
import org.workcraft.dependencymanager.advanced.user.SwingAutoRefreshExpression

object Expressions {
  case class Expression[+T](jexpr: JExpression[_ <: T]) {
    def unsafeEval: T = GlobalCache.eval(jexpr)
    def eval: IO[T] = ioPure.pure { unsafeEval }
    def lwmap[S] (f : T => S) : Expression[S] = Expression(new JExpression[S] {
      override def getValue(subscriber : Listener) : ValueHandleTuple[S] = { 
        val res = jexpr.getValue(subscriber)
        new ValueHandleTuple(f(res.value), res.handle)
      }
    })
  }

  case class ModifiableExpression[T](expr: Expression[T], set: T => IO[Unit]) {
    def unsafeAssign(from: Expression[T]) = GlobalCache.assign(this, from.jexpr)
    def update(f: T => T) = expr.eval >>= (v => set(f(v)))
    def assign(from: Expression[T]): IO[Unit] = ioPure.pure(unsafeAssign(from))
    
    def := (from: Expression[T]) = assign(from)
    def := (from: T) = set(from) // grief and woe

    def jexpr: JModifiableExpression[T] = {
      def sv(t: T) = set(t).unsafePerformIO
      jModifiableExpression((this: Expression[T]).jexpr, new Setter[T] {
        override def setValue(t: T) {
          sv(t)
        }
      })
    }
    
    def xmap[S](f: T => S)(g: S => T) = ModifiableExpression(expr.lwmap(f), set.contramap(g))
    
    def validate[E] (validator : T => Option[E]) : ModifiableExpressionWithValidation[T,E] = ModifiableExpressionWithValidation(expr, x => {
      validator(x) match {
        case None => set(x) >| None
        case Some(err) => ioPure.pure(Some(err))
      }
    })
  }

  class ThreadSafeVariable[T](initialValue: T) extends Variable[T](initialValue) {
    override def setValue(value: T) = {
      SwingUtilities.invokeAndWait(new Runnable() {
        def run = ThreadSafeVariable.super.setValue(value)
      })
    }
  }

  implicit def convertModifiableExpression[T](me: JModifiableExpression[T]) = ModifiableExpression[T](decorateExpression(me), x => ioPure.pure{me.setValue(x)})

  implicit object JExpressionMonad extends Monad[JExpression] {
    override def pure[A](x: => A) = jConstant(x)
    override def bind[A, B](a: JExpression[A], f: A => JExpression[B]): JExpression[B] = bindJ(a, asFunctionObject(f))
  }

  implicit def decorateExpression[T](e: JExpression[_ <: T]): Expression[T] = Expression[T](e)

  implicit def modifiableExpressionAsReadonly[T](m: ModifiableExpression[T]): Expression[T] = m.expr

  implicit object ExpressionMonad extends Monad[Expression] {
    override def fmap[A, B](fa: Expression[A], f: A => B): Expression[B] = fmapJ[A, B] (asFunctionObject(f), fa.jexpr)
    override def apply[A, B](f: Expression[A => B], a: Expression[A]): Expression[B] = fmapJ[A=>B, A, B](asFunctionObject2(_(_)), f.jexpr, a.jexpr) 
    override def pure[A](x: => A) = constant(x)
    override def bind[A, B](a: Expression[A], f: A => Expression[B]): Expression[B] = decorateExpression(bindJ[A, B](a.jexpr: JExpression[_ <: A], asFunctionObject(((_: Expression[B]).jexpr).compose(f))))
  }

  case class ModifiableExpressionWithValidation[T,E](expr: Expression[T], set: T => IO[Option[E]]) {
    def xmap[S](f : T => S)(g : S => Either[E, T]) = ModifiableExpressionWithValidation[S,E](expr.map(f), s => g(s) match{
      case Left(err) => ioPure.pure(Some(err))
      case Right(t) => set(t)
    })
  }
  
  def constant[T](x: T): Expression[T] = jConstant(x)

  /**
   *  Needed because Scala is stupid!
   */
  implicit def monadicSyntaxME[A](m: ModifiableExpression[A]) = new {
    def map[B](f: A => B) = implicitly[Monad[Expression]].fmap(m, f)
    def flatMap[B](f: A => Expression[B]) = implicitly[Monad[Expression]].bind(m, f)
    def >>=[B](f: A => Expression[B]) = implicitly[Monad[Expression]].bind(m, f)
  }

  implicit def monadicSyntaxV[A](m: Variable[A]) = monadicSyntaxME(m)
  implicit def monadicSyntaxJ[A](m: JModifiableExpression[A]) = monadicSyntaxME(m)

  trait ExpressionOps[+A] {
    def mapE[B](f: A => Expression[_ <: B]): Expression[List[B]]
  }

  implicit def augmentWithExpressionOps[A, Coll <: Iterable[A]](coll: Coll): ExpressionOps[A] = {
    new ExpressionOps[A]() {
      def mapE[B](f: A => Expression[_ <: B]) = {
        sequenceExpressions[B](coll.map(f))
      }
    }
  }

  implicit def implicitJexpr[T](e: Expression[T]): JExpression[_ <: T] = e.jexpr
  implicit def implicitMJexpr[T](e: ModifiableExpression[T]): JModifiableExpression[T] = e.jexpr

  def sequenceExpressions[A](collection: Iterable[Expression[_ <: A]]): Expression[List[A]] = collection.foldRight(constant(Nil: List[A]))((head: Expression[_ <: A], tail: Expression[List[A]]) => for (tail <- tail; head <- head) yield head :: tail)

  def newVar[T](x: T): IO[Variable[T]] = ioPure.pure(Variable.create(x))
  
  def evanescentAutoRefresh[T] (expr: Expression[T], update: T => IO[Unit]) = new AutoRefreshHandle (new AutoRefreshExpression {
    override def onEvaluate (context: EvaluationContext) = update(context.resolve(expr)).unsafePerformIO      
  })
  
  def swingAutoRefresh[T] (expr: Expression[T], update: T => IO[Unit]) = new AutoRefreshHandle (new SwingAutoRefreshExpression {
    override def onEvaluate (context: EvaluationContext) = update(context.resolve(expr)).unsafePerformIO 
  })
  
  object DisposableAutoRefreshExpression {
    val hashmap = scala.collection.mutable.HashSet[AutoRefreshExpression]()
  }
  
  class DisposableAutoRefreshExpression[T] (expr: Expression[T], update: T => IO[Unit]) extends AutoRefreshExpression {
       
    DisposableAutoRefreshExpression.hashmap += this
    
    private var disposeRequested = false
    
    override def onEvaluate (context: EvaluationContext) = {
      if (!disposeRequested)
    	  update(context.resolve(expr)).unsafePerformIO      
    }
    
    def dispose = {
      disposeRequested = true
      DisposableAutoRefreshExpression.hashmap -= this
    }
  }
  
  class AutoRefreshHandle (private val ref: AnyRef)
  
  def manualDisposeAutoRefresh[T] (expr: Expression[T], update: T => IO[Unit]): DisposableAutoRefreshExpression[T]  = new DisposableAutoRefreshExpression(expr, update)
  
  def treeFold[A](z: A)(f: (A, A) => A, l: List[A]): A = l match {
    case Nil => z
    case x :: Nil => f(z, x)
    case q @ (x :: xs) => treeFold(z)(f, q.grouped(2).map({
      case List(a, b) => f(a, b)
      case List(a) => a
      case _ => throw new RuntimeException("Should not happen")
    }).toList)
  }

  def treeSequence[A](l: List[Expression[A]]): Expression[List[A]] =
    treeFold[Expression[List[A]]](constant(List()))((q, p) => (q <**> p)(_ ++ _), l.map(_.lwmap(List(_))))  
}
