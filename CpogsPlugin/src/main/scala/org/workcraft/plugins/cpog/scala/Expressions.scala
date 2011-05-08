package org.workcraft.plugins.cpog.scala

import org.workcraft.dependencymanager.advanced.core.Expression
import org.workcraft.dependencymanager.advanced.core.Expressions.{joinCollection => joinCollectionJ, constant => constantJ, bind => bindJ, fmap => fmapJ}
import scala.collection.JavaConversions.{asJavaCollection, asScalaIterable}
import Util.asFunctionObject

object Expressions {
  implicit def monadicSyntax[A](m: Expression[A]) = new {
    def bind[B](x: A => Expression[B] ) = bindJ(m, asFunctionObject(x))

    def map[B](f: A => B) = fmapJ[A,B](asFunctionObject(f), m)

    def flatMap[B](f: A => Expression[B]) = bind(f)
  }
  
  def joinCollection[A](collection : Iterable[Expression[_ <: A]]) = for(javaRes <- joinCollectionJ[A] (asJavaCollection (collection))) yield asScalaIterable (javaRes)
  def constant[A](a : A) = constantJ(a)
}
