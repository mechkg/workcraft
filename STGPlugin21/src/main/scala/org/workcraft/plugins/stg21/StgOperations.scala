package org.workcraft.plugins.stg21

import types._
import java.awt.geom.Point2D
import org.workcraft.plugins.stg21.fields.Field
import org.workcraft.plugins.stg21.fields.VisualStgFields

object StgOperations {
  
import scalaz.State
import StateExtensions._

 val createMathPlace : State[MathStg, Id[Place]] =
    Col.add(Place(0)).on(MathStg.places)
  
  
  def createSignalTransition(where : Point2D) : State[VisualStg, Id[Transition]] = {
    null // TODO
  }
    
  def createDummyTransition(where : Point2D) : State[VisualStg, Id[Transition]] = {
    null // TODO
  }
  
  def createPlace(where : Point2D) : State[VisualStg, Id[Place]] ={
    import VisualStg._
    for(p <- createMathPlace.on(VisualStgFields.math);
      _ <- VisualModel.addNode[StgNode,Id[Arc]](PlaceNode(p), where).on(VisualStgFields.visual))
      yield p;
  }
  
  def removePlaceS(place : Id[Place]) : State[VisualStg, Unit] = {
    import VisualStg._
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
