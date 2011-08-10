package org.workcraft.plugins.stg21

import types._
import java.awt.geom.Point2D
import org.workcraft.plugins.stg21.fields.Field
import org.workcraft.plugins.stg21.fields.VisualStgFields
import org.workcraft.exceptions.NotImplementedException

object StgOperations {
  
import scalaz.State
import StateExtensions._

  val createMathPlace : State[MathStg, Id[Place]] =
    Col.add(Place(0)).on(MathStg.places)
  
  def createMathTransition(t : Transition) =
    Col.add(t).on(MathStg.transitions)
  
  val createNewSignal : State[MathStg, Id[Signal]] = Col.add(Signal("qwe", SignalType.Internal)).on(MathStg.signals)
  
  def createSignalTransition(where : Point2D) : State[VisualStg, Id[Transition]] = {
    for(
     t <- (for(
        sig <- createNewSignal;
        t <- createMathTransition(SignalTransition(sig, TransitionDirection.Toggle))) yield t).on(VisualStgFields.math);
    _ <- VisualModel.addNode[StgNode,Id[Arc]](TransitionNode(t), where).on(VisualStgFields.visual)
    ) yield t
  }
    
  def createDummyTransition(where : Point2D) : State[VisualStg, Id[Transition]] = {
    for(t <- createMathTransition(DummyTransition).on(VisualStgFields.math);
    _ <- VisualModel.addNode[StgNode,Id[Arc]](TransitionNode(t), where).on(VisualStgFields.visual)
    ) yield t
  }
  
  def createPlace(where : Point2D) : State[VisualStg, Id[Place]] ={
    for(p <- createMathPlace.on(VisualStgFields.math);
      _ <- VisualModel.addNode[StgNode,Id[Arc]](PlaceNode(p), where).on(VisualStgFields.visual))
      yield p;
  }
  
  def removePlaceS(place : Id[Place]) : State[VisualStg, Unit] = {
    for(_ <- Col.remove(place).on(MathStg.places).on(VisualStg.math);
      _ <- VisualModel.removeNode[StgNode, Id[Arc]](PlaceNode(place)).on(VisualStg.visual))
      yield ()
  }
  
  def removePlace(place : Id[Place]) (visualStg : VisualStg) : VisualStg = 
    VisualStg(
        visualStg.math.copy(places = visualStg.math.places.remove(place)),
        visualStg.visual.copy(nodesInfo = visualStg.visual.nodesInfo - PlaceNode(place))
        )
}
