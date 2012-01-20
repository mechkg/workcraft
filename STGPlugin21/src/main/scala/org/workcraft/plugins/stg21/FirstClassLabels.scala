package org.workcraft.plugins.stg21

import types._
import java.awt.geom.Point2D
import scalaz.Lens

object fields {
  
  trait VisualStgLenses {
	val math : Lens[VisualStg,MathStg] = Lens(_.math, (s,x) => s.copy(math=x))
	val visual : Lens[VisualStg, VisualModel[StgNode, Id[Arc]]] = Lens(_.visual, (s,x) => s.copy(visual=x))
  }
  object VisualStgLenses extends VisualStgLenses
  
  trait MathStgLenses {
    val signals : Lens[MathStg, Col[Signal]] = Lens (_.signals, (s,x) => s.copy(signals=x))
    val transitions : Lens[MathStg, Col[Transition]] = Lens (_.transitions, (s,x) => s.copy(transitions=x))
    val places : Lens[MathStg, Col[ExplicitPlace]] = Lens (_.places, (s,x) => s.copy(places=x))
    val arcs : Lens[MathStg, Col[Arc]] = Lens (_.arcs, (s,x) => s.copy(arcs=x))
  }
  
  trait VisualInfoLenses {
    val position : Lens[VisualInfo, Point2D.Double] = Lens ((_ : VisualInfo).position, (s,x) => s.copy(position=x))
    val parent : Lens[VisualInfo, Option[Id[Group]]] = Lens ((_ : VisualInfo).parent, (s,x) => s.copy(parent=x))
  }
  object VisualInfoLenses extends VisualInfoLenses 
  
  trait GroupLenses {
    val info : Lens[Group, VisualInfo] = Lens(g => g.info, (g,v) => g.copy(info=v))
  }
  object GroupLenses extends GroupLenses
  
  trait VisualModelLenses {
    def groups[N,A] : Lens[VisualModel[N,A], Col[Group]] = Lens (_.groups, (s,x) => s.copy(groups=x))
    def arcs[N,A] : Lens[VisualModel[N,A], Map[A, VisualArc]] = Lens (_.arcs, (s,x) => s.copy(arcs=x))
    def nodesInfo[N,A] : Lens[VisualModel[N,A], Map[N, VisualInfo]] = Lens (_.nodesInfo, (s,x) => s.copy(nodesInfo=x))
  }
  object VisualModelLenses extends VisualModelLenses
  
  trait Point2DLenses {
    def x : Lens[Point2D.Double, Double] = Lens(_.x, (p, x) => new Point2D.Double(x, p.y))
    def y : Lens[Point2D.Double, Double] = Lens(_.y, (p, y) => new Point2D.Double(p.x, y))
  }
  object Point2DLenses extends Point2DLenses
  
  trait ExplicitPlaceLenses {
    def initialMarking : Lens[ExplicitPlace, Int] = Lens(_.initialMarking, (p, x) => p.copy(initialMarking=x))
    def name : Lens[ExplicitPlace, String] = Lens(_.name, (p, x) => p.copy(name=x))
  }
  
  trait SignalLenses {
    def name : Lens[Signal, String] = Lens(_.name, (s,n) => s.copy(name=n))
    def direction : Lens[Signal, SignalType] = Lens(_.direction, (s,n) => s.copy(direction=n))
  }
}
