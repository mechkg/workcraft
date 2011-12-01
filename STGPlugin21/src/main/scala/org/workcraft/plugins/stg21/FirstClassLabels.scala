package org.workcraft.plugins.stg21

import types._
import java.awt.geom.Point2D

object fields {
  
trait Field[W,P] {
  val get : W => P
  val set : P => W => W
}

  
  def field[W,P] (g : W => P, s : P => W => W) = new Field[W,P] {
    val get = g
    val set = s
  }
  
  trait VisualStgFields {
	val math : Field[VisualStg,MathStg] = field(_.math, x => _.copy(math=x))
	val visual : Field[VisualStg, VisualModel[StgNode, Id[Arc]]] = field(_.visual, x => _.copy(visual=x))
  }
  object VisualStgFields extends VisualStgFields
  
  trait MathStgFields {
    val signals : Field[MathStg, Col[Signal]] = field (_.signals, x => _.copy(signals=x))
    val transitions : Field[MathStg, Col[Transition]] = field (_.transitions, x => _.copy(transitions=x))
    val places : Field[MathStg, Col[ExplicitPlace]] = field (_.places, x => _.copy(places=x))
    val arcs : Field[MathStg, Col[Arc]] = field (_.arcs, x => _.copy(arcs=x))
  }
  
  trait VisualInfoFields {
    val position : Field[VisualInfo, Point2D] = field ((_ : VisualInfo).position, x => _.copy(position=x))
  }
  object VisualInfoFields extends VisualInfoFields 
  
  trait VisualModelFields {
    def groups[N,A] : Field[VisualModel[N,A], Col[Group]] = field (_.groups, x => _.copy(groups=x))
    def arcs[N,A] : Field[VisualModel[N,A], Map[A, VisualArc]] = field (_.arcs, x => _.copy(arcs=x))
    def nodesInfo[N,A] : Field[VisualModel[N,A], Map[N, VisualInfo]] = field (_.nodesInfo, x => _.copy(nodesInfo=x))
  }
}