package org.workcraft.plugins

package object stg21 {

import java.awt.geom.Point2D
import org.workcraft.dom.visual.connections.RelativePoint
import scala.collection.mutable.WeakHashMap


object types {
  
  sealed trait SignalType
  object SignalType {
    case object Input extends SignalType
    case object Output extends SignalType
    case object Internal extends SignalType
  }

  sealed trait Transition
  case class DummyTransition extends Transition
  case class SignalTransition(signal : Id[Signal], direction : TransitionDirection) extends Transition
  
  
  case class Signal(name : String, direction : SignalType)
  case class Id[T] (id : Int)

  case class Place(initialMarking : Integer)
  
  sealed trait Arc
  class ConsumingArc(from : Id[Place], to : Id[Transition]) extends Arc
  class ProducingArc(from : Id[Transition], to : Id[Place]) extends Arc
  class ImplicitPlaceArc[M[_]](from : Id[Transition], to : Id[Transition], initialMarking : Integer) extends Arc

  case class Col[T] (map : Map[Id[T], T], nextFreeId : Id[T]) {
	  def remove(id : Id[T]) : Col[T] = Col[T](map - id, nextFreeId)
  }
  
  object Col {
	  def add(t : T) : State[Col[T], Id[T]] = state (col -> {
	    (Col[T](col.map + (col.nextFreeId, t), Id[T](col.nextFreeId.id + 1)), col.nextFreeId)
	  })  
  }

  case class MathStg (
    signals : Col[Signal],
    transitions : Col[Transition],
    places : Col[Place],
    arcs : Col[Arc]
  )
  
  sealed trait StgNode
  
  case class PlaceNode (p : Id[Place]) extends StgNode
  case class TransitionNode (t : Id[Transition]) extends StgNode
  
  class VisualArc

  case class Bezier(cp1: RelativePoint, cp2: RelativePoint) extends VisualArc
  case class Polyline(cp: List[Point2D]) extends VisualArc
  
  case class Group(info : VisualInfo)

  case class VisualInfo(position : Point2D, parent : Option[Id[Group]])
  
  case class VisualModel[N,A] (
    groups : Col[Group],
    arcs : Map[A, VisualArc],
    nodesInfo : Map[N, VisualInfo] 
  )
  
  case class VisualStg (
    stg : MathStg,
    visualStg : VisualModel[StgNode, Id[Arc]]
  )
  
  object MathStg {
    def onPlaces[T] (action : State[Col[Place], T]) : State[MathStg, T] = state (stg -> {
        val (newPlaces, x) = action(stg.places)
        (stg.copy(places = newPlaces), x)
      })
  }
}
}

/*

class VisualInfo[M[_]] (position : M[Point2D], group : Option[Group])



sealed trait TransitionDirection
object TransitionDirection {
	case object Plus extends TransitionDirection
	case object Minus extends TransitionDirection
	case object Toggles extends TransitionDirection
}

class Signal[M[_]](name : M[String], direction : M[SignalType])




    
case class Stg[M[_]](
    signals : M[List[Signal[M]]],
    transitions : M[List[Transition[M]]],
    places : M[List[Place[M]]],
    arcs : M[List[Arc[M]]]
 )

class VisualStg[M[_]] (stg : Stg[M], visual : WeakHashMap[VisualNode[M], VisualInfo[M]])

class Group

sealed trait VisualNode[M[_]]

case class TransitionNode[M[_]] (t : M[Transition[M]]) extends VisualNode[M]
case class PlaceNode[M[_]] (p : M[Place[M]]) extends VisualNode[M]
case class GroupNode[M[_]] (g : M[Group]) extends VisualNode[M]

}*/
