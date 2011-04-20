package org.workcraft.plugins.cpog.scala

import org.workcraft.dependencymanager.advanced.core.EvaluationContext
import org.workcraft.dependencymanager.advanced.core.GlobalCache
import org.workcraft.dependencymanager.advanced.user.ModifiableExpressionBase
import org.workcraft.util.FieldAccessor
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression
import org.workcraft.util.Maybe
import org.workcraft.plugins.cpog.Node
import org.workcraft.dependencymanager.advanced.core.Expressions.{fmap => javafmap, bind => javabind, asFunction, constant}

import org.workcraft.dependencymanager.advanced.core.Expression
import org.workcraft.util.Function
import org.workcraft.util.Function2
 
  
object Util {
	def asFunctionObject[T,R] (f : (T=>R)) = new Function[T,R] {
		def apply (x:T) = f(x)
	}
	
	def asFunctionObject2[T1,T2,R] (f : ((T1,T2)=>R)) = new Function2[T1,T2,R] {
		def apply (x:T1, y:T2) = f(x,y)
	}
	
	def withDefault[V] (default: V, f:(Node => Maybe[_ <: V])) : (Node => V) = { x => Maybe.Util.orElse (f(x), default) }
	
	def bindFunc[A, B] (a : Expression[_ <: A])(f : A => B) : Expression[B] = javafmap(asFunctionObject(f), a)
	def bindFunc[A, B, C] (a : Expression[_ <: A], b : Expression[_ <: B])(f : (A, B) => C) : Expression[C] = javafmap(asFunctionObject2(f), a, b)
	def fmap[A, B] (f : A => B)(a : Expression[_ <: A]) : Expression[B] = javafmap(asFunctionObject(f), a)
	def fmap[A, B, C] (f : (A, B) => C)(a : Expression[_ <: A], b : Expression[_ <: B]) : Expression[C] = javafmap(asFunctionObject2(f), a, b)
	def bind[A, B] (a: Expression[_ <: A], f : A => _ <: Expression[_ <: B]) : Expression [B] = javabind[A,B](a, asFunctionObject(f))
	
	def applyFieldAccessor[T, FieldT] (expr:ModifiableExpression[T], accessor:FieldAccessor[T, FieldT]) : ModifiableExpression[FieldT] =
	  new ModifiableExpressionBase[FieldT] {
		override def setValue(newValue:FieldT) = expr.setValue(accessor.assign(GlobalCache.eval(expr), newValue));
		override def evaluate (context:EvaluationContext) : FieldT = accessor(context.resolve(expr))
	}
}