package org.workcraft.plugins.stg21
import java.awt.geom.Point2D
import scalaz.State
import org.workcraft.scala.Expressions._
import scalaz.Lens

object modifiable {
  import org.workcraft.plugins.stg21.fields._
  implicit def decorateModifiableExpression[W](we : ModifiableExpression[W]) = new {
    def modifiableField[P]
      (getter : W => P)
      (setter : (W,P) => W)
      : ModifiableExpression[P] = {
      ModifiableExpression (for(w <- we) yield getter(w)
      , (v : P) => we.update(setter(_,v)))
    }
    def refract[P] (field : Lens[W,P]) : ModifiableExpression[P] = modifiableField[P](field.get)(field.set)
    def applyIso[V](to : W => V, from : V => W) : ModifiableExpression[V] = modifiableField[V](to)((w,v)=>from(v))
    def runState[R](state : State[W,R]) : R = {
      val (nw,r) = state(we.unsafeEval)
      we.setValue(nw)
      r
    }
  }
  import types._
  implicit def decorateModifiableVisualStg (stg : ModifiableExpression[VisualStg]) = new {
    val visual = stg.refract (VisualStg.visual)
    val math = stg.refract (VisualStg.math)
  }
  implicit def decorateModifiableMathStg (stg : ModifiableExpression[MathStg]) = new {
    val signals : ModifiableExpression[Col[Signal]] = stg.refract(MathStg.signals)
    val transitions : ModifiableExpression[Col[Transition]] = stg.refract(MathStg.transitions)
    val places : ModifiableExpression[Col[ExplicitPlace]] = stg.refract(MathStg.places)
    val arcs : ModifiableExpression[Col[Arc]] = stg.refract(MathStg.arcs)
  }
  implicit def decorateModifiableVisualModel[N,A] (visual : ModifiableExpression[VisualModel[N,A]]) = new {
    val groups : ModifiableExpression[Col[Group]] = visual.refract (VisualModel.groups[N,A])
    val arcs : ModifiableExpression[Map[A, VisualArc]] = visual.refract(VisualModel.arcs[N,A])
    val nodesInfo : ModifiableExpression[Map[N, VisualInfo]] = visual.refract(VisualModel.nodesInfo[N,A])
  }

  class DecoratedModifiableMap[K,V] (map : ModifiableExpression[Map[K,V]]) {
    def lookup(key : K) : ModifiableExpression[Option[V]] = { 
      map.modifiableField ((_ : Map[K,V]).get(key)) ((s, x : Option[V]) => x match {
        case None => s - key
        case Some(value) => s + ((key, value))
      }
      )
    }
  }
  
  implicit def decorateModifiableMap[K,V] (map : ModifiableExpression[Map[K,V]]) = new DecoratedModifiableMap[K,V](map)
  
  class DecoratedModifiableOption[V](o : ModifiableExpression[Option[V]]) {
    def orElse(v : V) : ModifiableExpression[V] = o.modifiableField ((_ : Option[V]).getOrElse(v)) ((_,x) => Some(x))
  }
  implicit def decorateModifiableOption[V] (o : ModifiableExpression[Option[V]]) = new DecoratedModifiableOption[V](o)
  
  implicit def decorateVisualInfo (v : ModifiableExpression[VisualInfo]) = new {
    def position : ModifiableExpression[Point2D.Double] = v.refract(VisualInfo.position)
    def parent : ModifiableExpression[Option[Id[Group]]] = v.refract(VisualInfo.parent)
  }
  implicit def decorateCol[T] (v : ModifiableExpression[Col[T]]) = new {
    def lookup(k : Id[T]) : ModifiableExpression[Option[T]] = v.modifiableField (_.lookup(k)) ((m,x) => x match {
      case Some(t) => m.insert(k)(t)
      case None => m
    })
  }
}
