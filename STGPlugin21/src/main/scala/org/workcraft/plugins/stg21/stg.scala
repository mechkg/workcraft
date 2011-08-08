
package org.workcraft.plugins

package stg21 {

import org.workcraft.util.Maybe
import org.workcraft.dependencymanager.advanced.core.EvaluationContext
import org.workcraft.dependencymanager.advanced.user.ModifiableExpressionBase
import org.workcraft.dependencymanager.advanced.user.ModifiableExpression

import java.awt.geom.Point2D
import org.workcraft.dom.visual.connections.RelativePoint
import scala.collection.mutable.WeakHashMap
import scalaz.State

object types {
  
  sealed trait SignalType
  object SignalType {
    case object Input extends SignalType
    case object Output extends SignalType
    case object Internal extends SignalType
  }
  
  sealed trait TransitionDirection
  
  object TransitionDirection {
    case object Rise extends TransitionDirection
    case object Fall extends TransitionDirection
    case object Toggle extends TransitionDirection
  }

  sealed trait Transition
  case object DummyTransition extends Transition
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
	  def lookup(key : Id[T]) : Option[T] = map.get(key)
	  def insert(key : Id[T])(value : T) = copy(map = map + ((key, value)))
	  def keys : List[Id[T]] = map.keys.toList
  }

  import StateExtensions._
  
  object Col {
    def empty[T] = Col[T](Map.empty, Id[T](0))
    def add[T](t : T) : State[Col[T], Id[T]] = state (col => {
      (Col[T](col.map + ((col.nextFreeId, t)), Id[T](col.nextFreeId.id + 1)), col.nextFreeId)
    })
    def remove[T](t : Id[T]) : State[Col[T], Boolean] = state (col => {
      (Col[T](col.map - t, col.nextFreeId), col.map.contains(t))
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
  
  sealed trait VisualNode
  case class StgVisualNode(n : StgNode) extends VisualNode
  case class GroupVisualNode(g : Id[Group]) extends VisualNode
  
  case class VisualModel[N,A] (
    groups : Col[Group],
    arcs : Map[A, VisualArc],
    nodesInfo : Map[N, VisualInfo] 
  )
  
  case class VisualStg (
    math : MathStg,
    visual : VisualModel[StgNode, Id[Arc]]
  )

  object VisualStg extends fields.VisualStgFields {
	val empty = VisualStg(MathStg.empty, VisualModel.empty)
  }
  
  object VisualModel extends fields.VisualModelFields {
    def empty[N,A] = VisualModel[N,A](Col.empty, Map.empty, Map.empty)
    def addNode[N,A](node : N, where : Point2D) : State[VisualModel[N,A], Unit] = state ((m : Map[N, VisualInfo]) => (m + ((node, VisualInfo(where, None))), ())) .on(nodesInfo)
    def removeNode[N,A](node : N) : State[VisualModel[N,A], Boolean] = state ((m : Map[N, VisualInfo]) => (m - node, m.contains(node))) .on(nodesInfo)
  }
  
  object MathStg extends org.workcraft.plugins.stg21.fields.MathStgFields {
    val empty = MathStg(Col.empty, Col.empty, Col.empty, Col.empty)
  }
}
}
