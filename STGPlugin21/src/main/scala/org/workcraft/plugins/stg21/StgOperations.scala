package org.workcraft.plugins.stg21

import types._
import java.awt.geom.Point2D
import org.workcraft.plugins.stg21.fields.VisualStgLenses
import org.workcraft.exceptions.NotImplementedException
import scalaz.Lens

object StgOperations {
  
import scalaz.State
import StateExtensions._

  def createMathPlace(name : String) : State[MathStg, Id[ExplicitPlace]] =
    Col.add(ExplicitPlace(0, name)).on(MathStg.places)
  
  def findFree(cur : Int, l : List[Int]) : Int = l match {
  case Nil => cur
  case (h :: t) => if (h > cur) cur else if (h<cur) findFree(cur, t) else findFree(cur+1, t)
  }
    
  def allocInstanceNumber(transitions : List[Transition], label : TransitionLabel) = {
    findFree(0, transitions.filter(t => t._1 == label).map(_._2).sorted)
  }
    
  def createMathTransition(t : Transition) =
    Col.add(t).on(MathStg.transitions)
    
  def createMathTransition(t : TransitionLabel) =
    state ((col : Col[Transition]) => Col.add((t, allocInstanceNumber(col.map.values.toList, t)))(col)).on(MathStg.transitions)
  
  val createNewSignal : State[MathStg, Id[Signal]] = Col.add(Signal("x", SignalType.Internal)).on(MathStg.signals)
  
  def createSignalTransition(where : Point2D.Double) : State[VisualStg, Id[Transition]] = {
    for(
     t <- (for(
        sig <- createNewSignal;
        t <- createMathTransition(SignalLabel(sig, TransitionDirection.Toggle))) yield t).on(VisualStgLenses.math);
    _ <- VisualModel.addNode[StgNode,Id[Arc]](TransitionNode(t), where).on(VisualStgLenses.visual)
    ) yield t
  }
    
  def createDummyTransition(where : Point2D.Double, name : String) : State[VisualStg, Id[Transition]] = {
    for(t <- createMathTransition(DummyLabel(name)).on(VisualStgLenses.math);
    _ <- VisualModel.addNode[StgNode,Id[Arc]](TransitionNode(t), where).on(VisualStgLenses.visual)
    ) yield t
  }
  
  def createPlace(where : Point2D.Double, name : String) : State[VisualStg, Id[ExplicitPlace]] ={
    for(p <- createMathPlace(name).on(VisualStgLenses.math);
      _ <- VisualModel.addNode[StgNode,Id[Arc]](ExplicitPlaceNode(p), where).on(VisualStgLenses.visual))
      yield p;
  }
  
  def removePlaceS(place : Id[ExplicitPlace]) : State[VisualStg, Unit] = {
    for(_ <- Col.remove(place).on(MathStg.places).on(VisualStg.math);
      _ <- VisualModel.removeNode[StgNode, Id[Arc]](ExplicitPlaceNode(place)).on(VisualStg.visual))
      yield ()
  }
  
  def removePlace(place : Id[ExplicitPlace]) (visualStg : VisualStg) : VisualStg = 
    VisualStg(
        visualStg.math.copy(places = visualStg.math.places.remove(place)),
        visualStg.visual.copy(nodesInfo = visualStg.visual.nodesInfo - ExplicitPlaceNode(place))
        )
        
   def connectTransitions(t1 : Id[Transition], t2 : Id[Transition]) : State[MathStg, Id[Arc]] = Col.add[Arc](ImplicitPlaceArc(t1, t2, 0)) on MathStg.arcs
   def connectTP(t : Id[Transition], p : Id[ExplicitPlace]) : State[MathStg, Id[Arc]] = Col.add[Arc](ProducingArc(t,p)) on MathStg.arcs
   def connectPT(p : Id[ExplicitPlace], t : Id[Transition]) : State[MathStg, Id[Arc]] = Col.add[Arc](ConsumingArc(p, t)) on MathStg.arcs
}
